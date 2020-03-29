package explodingKittens.cards

import explodingKittens.game.ExplodingKittens
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Shuffle(id: Int) : Card(id) {
    override val description: String = "Shuffles the deck"

    override suspend fun ExplodingKittens.play() {
        state.deck.shuffle()
    }
}