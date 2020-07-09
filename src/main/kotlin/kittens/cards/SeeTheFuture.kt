package kittens.cards

import io.ktor.util.KtorExperimentalAPI
import kittens.game.ExplodingKittens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.sendDM

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class SeeTheFuture(id: Int) : Card(id) {
    override val description: String = "Look at the top three cards in the deck"

    override suspend fun ExplodingKittens.play() {
        val top3 = state.deck.take(3)
        state.currentPlayer.user.sendDM {
            title = "The Future"
            description = "(topmost first): \n" + top3.joinToString(separator = "\n")
        }
    }
}