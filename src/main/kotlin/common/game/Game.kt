package common.game

import common.commands.State
import common.commands.states
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
                game.channel.states += game.type.commandState
                if (GameType.values().map { it.commandState }.all { it in game.channel.states }) {
                    game.channel.states -= State.Setup
                }
                game.startGame()
            }.onFailure { e ->
                println("Error in game ${game.type.name} in channel ${game.channel.name}")
                e.printStackTrace()
                endAndRemove(game.channel, game.type)
            }
        }

        private val games: MutableMap<Channel, MutableMap<GameType, Game>> = mutableMapOf()

        suspend fun endAndRemove(channel: Channel, gameType: GameType) {
            games[channel]?.get(gameType)?.stopGame()
            games[channel]?.remove(gameType)
            channel.states -= gameType.commandState
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
