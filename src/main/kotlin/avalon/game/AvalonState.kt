package avalon.game

import common.game.Game
import common.game.GameType
import common.game.Setup
import common.game.State
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class AvalonState(setup: Setup) : State<AvalonPlayer>(setup) {
    private val config = setup.config as AvalonConfig

    val roles = config.roles
    val randomRoles = config.randomRoles

    val leader get() = players[leaderNum % players.size]

    lateinit var rounds: Rounds; internal set
    var numEvil = 0
    var ladyEnabled = config.ladyEnabled

    //    internal var party: Set<AvalonPlayer>? = null
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

    override fun toString(): String {
        return "AvalonState(players=$players, roles=$roles, randomRoles=$randomRoles, numEvil=$numEvil, ladyEnabled=$ladyEnabled, ladyTarget=$ladyTarget, pastLadies=${pastLadies.map { it.name }}, roundNum=$roundNum, leaderNum=$leaderNum, goodWins=$goodWins, evilWins=$evilWins, rejectedQuests=$rejectedQuests, ladyOfTheLake=$ladyOfTheLake)"
    }

    companion object {
        fun inChannel(channel: Channel) = Game[channel, GameType.Avalon]?.state as? AvalonState
    }
}