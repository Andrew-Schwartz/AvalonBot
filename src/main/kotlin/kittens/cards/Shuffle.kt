package kittens.cards

import io.ktor.util.*
import kittens.game.ExplodingKittens
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Shuffle(id: Int) : Card(id) {
    override val description: String = "Shuffles the deck"

    override suspend fun ExplodingKittens.play() {
        state.deck.shuffle()
    }
}