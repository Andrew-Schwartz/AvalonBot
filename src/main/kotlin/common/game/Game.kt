package common.game

import common.commands.State
import common.commands.states
import common.commands.subStates
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import lib.dsl.Bot
import lib.dsl.embed
import lib.dsl.send
import lib.model.Color.Companion.red
import lib.model.IntoId
import lib.model.UserId
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin
import lib.util.log
import java.time.Duration

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
abstract class Game(val type: GameType, val setup: Setup) {
    abstract val state: common.game.State<*>
    val channel = setup.channel
    val pinnedMessages: ArrayList<Message> = arrayListOf()
    var running = false

    protected abstract suspend fun runGame(): GameFinish

    companion object {
        suspend fun runGame(game: Game) {
            Bot.launch {
                runCatching {
                    game.running = true
                    game.channel.states += game.type.states.commandState
                    game.channel.states += State.Game
                    game.channel.states -= State.Setup.Setup
                    game.state.players.map { it.user }.forEach {
                        userGames.putIfAbsent(it.id, mutableListOf())
                        userGames[it.id]?.add(game)
                    }
                    launch {
                        while (game.running) {
                            // TODO honestly the way this should work is the cache should have time limit for each
                            //  object and re-fetch it if its been more than a minute.
                            //  In this case, player isn't cached, so don't actually need to change this code
                            //  this is just a good reminder/place to put this to do
                            log("Updating players in $game")
                            game.state.players.forEach {
                                it.updateUser()
                            }
                            delay(Duration.ofMinutes(1))
                        }
                    }
                    game.runGame()
                }.onFailure { e ->
                    log("Error in game ${game.type.name} in channel ${game.channel.name}")
                    e.printStackTrace()
                    endAndRemove(game.channel, game.type, GameFinish(embed {
                        title = "ERROR in game ${game.type.name}"
                        description = e.message
                        color = red
                    }))
                }.onSuccess { info ->
                    log("${game.type.name} in channel ${game.channel.name} ended normally")
                    endAndRemove(game.channel, game.type, info)
                }
            }
        }

        internal val games: MutableMap<Channel, MutableMap<GameType, Game>> = mutableMapOf()
        private val userGames: MutableMap<UserId, MutableList<Game>> = mutableMapOf()

        suspend fun endAndRemove(channel: Channel, gameType: GameType, info: GameFinish) {
            channel.send(embed = info.message)
            games[channel]?.get(gameType)?.run {
                running = false
                state.players.map { it.user }.forEach {
                    userGames[it.id]?.remove(this)
                }
                // toList to prevent concurrentModificationException
                Bot.launch {
                    pinnedMessages.toList().forEach { pin ->
                        pinnedMessages -= pin
                        runCatching { deletePin(pin.channelId, pin) }
                                .onFailure { println(it.message) }
                    }
                }
                setup.restart()
            }
            games[channel]?.remove(gameType)
            channel.states -= gameType.states.commandState
            channel.states -= State.Game
            channel.states.removeAll(subStates(gameType.states.parentClass))
            channel.states += State.Setup.Setup
        }

        /**
         * All of the games that [user] is in
         */
        fun forUser(user: IntoId<UserId>): List<Game> {
            return userGames.getOrDefault(user.intoId(), listOf()).distinct()
        }

        /**
         * Gets the active game of type [gameType] in channel [channel], or null if there is no
         * game of that type in that channel
         */
        operator fun get(channel: Channel, gameType: GameType): Game? {
            return games[channel]?.get(gameType)
        }

        /**
         * If there is an instance of [gameType] in this [channel], end it
         * Set the game in this channel to [game]
         */
        operator fun set(channel: Channel, gameType: GameType, game: Game) {
            games[channel]?.get(gameType)
            games.getOrPut(channel) { mutableMapOf() }[gameType] = game
        }
    }
}
