package common.game

import common.bot
import common.commands.State
import common.commands.states
import common.commands.subStates
import common.util.now
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import lib.model.Color.Companion.red
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin
import java.time.Duration

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
abstract class Game(val type: GameType, setup: Setup) {
    abstract val state: common.game.State<*>
    val channel = setup.channel
    val pinnedMessages: ArrayList<Message> = arrayListOf()
    var running = false

    protected abstract suspend fun startGame(): GameFinish

    abstract suspend fun stopGame(info: GameFinish)

    companion object {
        suspend fun startGame(game: Game) {
            GlobalScope.launch {
                runCatching {
                    game.running = true
                    game.channel.states += game.type.states.commandState
                    game.channel.states += State.Game
                    game.channel.states -= State.Setup.Setup
                    launch {
                        while (game.running) {
                            delay(Duration.ofMinutes(1))
                            // TODO update player names
                        }
                    }
                    game.startGame()
                }.onFailure { e ->
                    println("[${now()}] Error in game ${game.type.name} in channel ${game.channel.name}")
                    e.printStackTrace()
                    endAndRemove(game.channel, game.type, GameFinish {
                        title = "Error in Error in game ${game.type.name}"
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

        suspend fun endAndRemove(channel: Channel, gameType: GameType, info: GameFinish) {
            with(bot) {
                channel.send(embed = info.message)
                games[channel]?.get(gameType)?.run {
                    stopGame(info)
                    running = false
                    pinnedMessages.forEach { pin ->
                        pinnedMessages -= pin
                        runCatching { deletePin(pin.channelId, pin) }
                                .onFailure { println(it.message) }
                    }
                }
            }
            games[channel]?.remove(gameType)
            channel.states -= gameType.states.commandState
            channel.states -= State.Game
            channel.states.removeAll(subStates(gameType.states.parentClass)) // TODO does this work
            channel.states += State.Setup.Setup
        }

        operator fun get(channel: Channel, gameType: GameType): Game =
                games.getOrPut(channel) { mutableMapOf() }
                        .getOrPut(gameType) {
                            val setup = Setup[channel, gameType]
                            gameType.game(setup)
                        }
    }
}
