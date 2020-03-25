package explodingKittens

import explodingKittens.cards.Card
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenState(val game: ExplodingKittens) {
    var currentPlayerIndex = 0

    val players: List<KittenPlayer>
        get() = game.gamePlayers.map { it as KittenPlayer }
    val userPlayerMap: Map<User, KittenPlayer>
        get() = game.userPlayerMap.mapValues { it as KittenPlayer }

    val currentPlayer
        get() = players[currentPlayerIndex]
    val nextPlayer
        get() = players[next(currentPlayerIndex)]

    val deck = arrayListOf<Card>()
    var turnOrder = 1

    suspend fun drawCard() {
        val card = deck.removeAt(0)
        card.run { draw() }
    }

    fun endTurn() {
        currentPlayerIndex = next(currentPlayerIndex)
    }

    private fun next(index: Int): Int {
        var index = index
        index += turnOrder
        index %= game.gamePlayers.size
        if (index < 0)
            index += 5
        return index
    }
}
