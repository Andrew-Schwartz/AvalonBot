package explodingKittens.cards

import explodingKittens.game.ExplodingKittens
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Skip(id: Int) : Card(id) {
    override val description: String = "End your turn without drawing a card"

    override suspend fun ExplodingKittens.play() {
        state.currentPlayer.numTurns--
        endTurn()
    }
}