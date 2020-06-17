package common.game

import common.bot
import common.commands.State
import common.commands.states
import common.util.now
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.model.Color.Companion.red
import lib.model.channel.Channel
import lib.model.channel.Message

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
abstract class Game(val type: GameType, setup: Setup) {
    val channel = setup.channel
    val pinnedMessages: ArrayList<Message> = arrayListOf()
    var started = false

    protected abstract suspend fun startGame(): GameFinish

    abstract suspend fun stopGame(info: GameFinish)

    companion object {
        suspend fun startGame(game: Game) {
            GlobalScope.launch {
                runCatching {
                    game.started = true
                    game.channel.states += game.type.commandState
                    game.channel.states -= State.Setup
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
            }
            games[channel]?.get(gameType)?.stopGame(info)
            games[channel]?.remove(gameType)
            channel.states -= gameType.commandState
//            channel.states.removeAll(subStates(gameType.))
            channel.states += State.Setup
        }

        operator fun get(channel: Channel, gameType: GameType): Game =
                games.getOrPut(channel) { mutableMapOf() }
                        .getOrPut(gameType) {
                            val setup = Setup[channel, gameType]
                            gameType.game(setup)
                        }
    }
}
