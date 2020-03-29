package common.game

import common.commands.CommandState
import common.commands.commandState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel
import lib.model.channel.Message

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
abstract class Game(val type: GameType, setup: Setup) {
    val channel = setup.channel
    val pinnedMessages: ArrayList<Message> = arrayListOf()
    var started = false

    protected abstract suspend fun startGame()

    abstract suspend fun stopGame()

    companion object {
        suspend fun startGame(game: Game) {
            runCatching {
                game.started = true
                game.startGame()
            }.onFailure { e ->
                println("Error in game ${game.type.name} in channel ${game.channel.name}")
                e.printStackTrace()
                game.stopGame()
                endAndRemove(game.channel, game.type)
            }
        }

        private val games: MutableMap<Channel, MutableMap<GameType, Game>> = mutableMapOf()

        suspend fun endAndRemove(channel: Channel, gameType: GameType) {
            games[channel]?.get(gameType)?.stopGame()
            games[channel]?.remove(gameType)
            channel.commandState = CommandState.Setup
        }

        operator fun get(channel: Channel, gameType: GameType): Game =
                games.getOrPut(channel) { mutableMapOf() }
                        .getOrPut(gameType) {
                            val setup = Setup[channel, gameType]
                            gameType.game(setup)
                        }
//                games.computeIfAbsent(channel) {
//                    val setup = Setup[channel, gameType]
//                    mutableMapOf(gameType to gameType.game(setup))
//                }[gameType]!!
    }
}
