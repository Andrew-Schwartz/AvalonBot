package explodingKittens.cards

import explodingKittens.KittenState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Skip(id: Int) : Card(id) {
    override val description: String = "End your turn without drawing a card"

    override suspend fun KittenState.play() {
        currentPlayer.toDraw--
        endTurn()
    }
}