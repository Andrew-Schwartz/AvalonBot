package main.game

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
abstract class Game(type: GameType, setup: Setup) {
    val channel = setup.channel

    abstract suspend fun startGame()

    fun error() {
        TODO("Stop this game in this channel instead of the whole bot")
    }

    companion object {
        private val games: MutableMap<Channel, MutableMap<GameType, Game>> = mutableMapOf()

        fun remove(channel: Channel, game: GameType) {
            games[channel]?.remove(game)
        }

        operator fun get(channel: Channel, game: GameType): Game =
                games.computeIfAbsent(channel) {
                    val setup = Setup[channel, game]
                    mutableMapOf(game to game.getGame(setup))
                }[game]!!
    }
}
