package kittens.game

import common.game.Setup
import common.game.State
import io.ktor.util.KtorExperimentalAPI
import kittens.cards.Card
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenState(setup: Setup) : State<KittenPlayer>() {
    init {
        Setup.remove(setup)
    }

    var currentPlayerIndex = 0

    override val players: ArrayList<KittenPlayer> = setup.players.map { it as KittenPlayer } as ArrayList<KittenPlayer>
    val userPlayerMap: Map<User, KittenPlayer> = players.associateBy { it.user }

    val currentPlayer
        get() = players[currentPlayerIndex]
    val nextPlayer
        get() = players[next(currentPlayerIndex)]

    val deck = arrayListOf<Card>()
    var turnOrder = 1

    fun next(index: Int): Int {
        var index = index
        index += turnOrder
        index %= players.size
        if (index < 0)
            index += 5
        return index
    }
}
