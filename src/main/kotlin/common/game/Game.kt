package common.game

import common.commands.State
import common.commands.states
import common.commands.subStates
import common.util.now
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import lib.dsl.send
import lib.model.Color.Companion.red
import lib.model.IntoId
import lib.model.UserId
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin
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
            GlobalScope.launch {
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
                            delay(Duration.ofMinutes(1))
                            game.state.players.forEach {
                                it.updateUser()
                            }
                        }
                    }
                    game.runGame()
                }.onFailure { e ->
                    println("[${now()}] Error in game ${game.type.name} in channel ${game.channel.name}")
                    e.printStackTrace()
                    endAndRemove(game.channel, game.type, GameFinish {
                        title = "ERROR in game ${game.type.name}"
                        description = e.message
                        color = red
                    })
                }.onSuccess { info ->
                    println("[${now()}] ${game.type.name} in channel ${game.channel.name} ended normally")
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
                pinnedMessages.toList().forEach { pin ->
                    pinnedMessages -= pin
                    runCatching { deletePin(pin.channelId, pin) }
                            .onFailure { println(it.message) }
                }
                setup.restart()
            }
            games[channel]?.remove(gameType)
            channel.states -= gameType.states.commandState
            channel.states -= State.Game
            channel.states.removeAll(subStates(gameType.states.parentClass))
            channel.states += State.Setup.Setup
        }

        fun forUser(user: IntoId<UserId>): List<Game> {
            return userGames.getOrDefault(user.intoId(), listOf<Game>()).distinct()
        }

        operator fun invoke(channel: Channel, gameType: GameType): Game {
            games.getOrPut(channel) { mutableMapOf() }[gameType] = gameType.game(Setup[channel, gameType])
            return games[channel]!![gameType]!!
        }

        operator fun get(channel: Channel, gameType: GameType): Game? {
            return games[channel]?.get(gameType)
        }
    }
}
