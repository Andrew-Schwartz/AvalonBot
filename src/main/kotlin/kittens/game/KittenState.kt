package kittens.game

import common.game.Setup
import kittens.cards.Card
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenState(val game: ExplodingKittens, setup: Setup) {
    init {
        Setup.remove(setup)
    }

    var currentPlayerIndex = 0

    val players: ArrayList<KittenPlayer> = setup.players.map { it as KittenPlayer } as ArrayList<KittenPlayer>
    val userPlayerMap: Map<User, KittenPlayer> = players.associateBy { it.user }

    val currentPlayer
        get() = players[currentPlayerIndex]
    val nextPlayer
        get() = players[game.next(currentPlayerIndex)]

    val deck = arrayListOf<Card>()
    var turnOrder = 1
}
