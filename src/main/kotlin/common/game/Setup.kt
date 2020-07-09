package common.game

import common.util.Vote
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.guild
import lib.model.channel.Channel
import lib.model.user.User
import lib.rest.http.httpRequests.getChannel

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Setup private constructor(
        val channel: Channel,
        private val gameType: GameType,
        val config: GameConfig,
        val players: MutableList<Player> = mutableListOf()
) {
    var startVote: Vote? = null

    suspend fun addPlayer(user: User) {
        players += gameType.player(user, channel.guild())
    }

    fun removePlayer(user: User) {
        players.removeIf { it.user == user }
    }

    operator fun contains(user: User) = players.any { it.user == user }

    override fun toString(): String = "Setup(channel=${channel.name},gameType=$gameType,config=$config,players=$players)"

    suspend fun restart() {
        val new = Setup(
                getChannel(channel, forceRequest = true),
                gameType,
                config.apply { reset() },
                players.map { it.apply { reset() } }.toMutableList()
        )
        setups.getOrPut(channel) { mutableMapOf() }[gameType] = new
    }

    companion object {
        internal val setups: MutableMap<Channel, MutableMap<GameType, Setup>> = mutableMapOf()

        fun remove(setup: Setup) = remove(setup.channel, setup.gameType)

        fun remove(channel: Channel, gameType: GameType) {
            setups[channel]?.remove(gameType)
        }

        operator fun get(channel: Channel, gameType: GameType): Setup =
                setups.getOrPut(channel) { mutableMapOf() }
                        .getOrPut(gameType) { Setup(channel, gameType, gameType.config) }
//
//        operator fun get(channel: Channel, gameType: GameType): Setup? =
//            setups[channel]?.get(gameType)
    }
}
