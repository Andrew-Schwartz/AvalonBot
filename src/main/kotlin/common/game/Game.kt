package common.game

import common.commands.Command
import common.commands.CommandState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
abstract class Game(val type: GameType, setup: Setup) {
    val channel = setup.channel

    protected abstract suspend fun startGame()

    abstract suspend fun stopGame()

//    suspend fun error(message: String? = null) {
//        stopGame()
//        remove(channel, type)
//        println("Error in game ${type.name} in channel ${channel.name}")
//        if (message != null)
//            println("error: $message")
//        throw GameException(message)
//    }
//
//    suspend fun <T> T?.nn(): T = this ?: error("null variable")

    companion object {
        suspend fun startGame(game: Game) {
            runCatching {
                game.startGame()
            }.onFailure { e ->
                println("Error in game ${game.type.name} in channel ${game.channel.name}")
                e.printStackTrace()
                game.stopGame()
                remove(game.channel, game.type)
            }
        }

        private val games: MutableMap<Channel, MutableMap<GameType, Game>> = mutableMapOf()

        fun remove(channel: Channel, game: GameType) {
            games[channel]?.remove(game)
            with(Command) { channel.commandState = CommandState.Setup }
        }

        operator fun get(channel: Channel, gameType: GameType): Game =
                games.computeIfAbsent(channel) {
                    val setup = Setup[channel, gameType]
                    mutableMapOf(gameType to gameType.game(setup))
                }[gameType]!!
    }
}
