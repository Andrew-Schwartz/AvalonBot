package explodingKittens.cards

import explodingKittens.KittenState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class SeeTheFuture(id: Int) : Card(id) {
    override val description: String = "Look at the top three cards in the deck"

    override suspend fun KittenState.play() {
        val player = currentPlayer
        with(game.bot) {
            val top3 = deck.take(3)
            player.user.sendDM {
                title = "The Future"
                description = "(topmost first): \n" + top3.joinToString(separator = "\n")
            }
        }
    }
}