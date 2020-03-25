package explodingKittens.cards

import explodingKittens.KittenState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Shuffle(id: Int) : Card(id) {
    override val description: String = "Shuffles the deck"

    override suspend fun KittenState.play() {
        deck.shuffle()
    }
}