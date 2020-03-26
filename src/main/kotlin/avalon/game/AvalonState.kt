package avalon.game

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User
import main.game.Setup

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class AvalonState(val game: Avalon, setup: Setup) {
    init {
        Setup.remove(setup)
    }

    val players: ArrayList<AvalonPlayer> = setup.players.map { it as AvalonPlayer } as ArrayList<AvalonPlayer>
    val userPlayerMap: Map<User, AvalonPlayer> = players.associateBy { it.user }

    val leader get() = players[leaderNum % players.size]

    lateinit var rounds: Rounds; internal set
    var numEvil = 0
    var ladyEnabled = false

    internal var party: Set<AvalonPlayer>? = null
    internal var ladyTarget: AvalonPlayer? = null
        set(value) {
            value?.let { pastLadies += it }
            field = value
        }
    internal val pastLadies: MutableList<AvalonPlayer> = mutableListOf()

    var roundNum = 1; internal set
    internal var leaderNum = 0
    var goodWins = 0; internal set
    var evilWins = 0; internal set
    internal var rejectedQuests = 0
    internal var ladyOfTheLake: AvalonPlayer? = null
}