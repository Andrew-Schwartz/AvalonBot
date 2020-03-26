package common.game

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Setup private constructor(val channel: Channel, private val gameType: GameType, val config: GameConfig) {
    val players: ArrayList<Player> = arrayListOf()

    fun addPlayer(user: User) {
        players += gameType.player(user)
    }

    fun removePlayer(user: User) {
        players.removeIf { it.user == user }
    }

    operator fun contains(user: User) = gameType.player(user) in players

    companion object {
        private val setups: MutableMap<Channel, MutableMap<GameType, Setup>> = mutableMapOf()

        fun remove(setup: Setup) {
            setups[setup.channel]?.remove(setup.gameType)
        }

        operator fun get(channel: Channel, game: GameType): Setup =
                setups.computeIfAbsent(channel) {
                    mutableMapOf(game to Setup(it, game, game.data()))
                }[game]!!
    }
}
