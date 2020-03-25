package explodingKittens.cards

import explodingKittens.KittenState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Attack(id: Int) : Card(id) {
    override val description: String = "End your turn without drawing a card and force the next player to take two turns in a row"

    override suspend fun KittenState.play() {
//        nextPlayer.toDraw += currentPlayer.toDraw
        nextPlayer.toDraw = 2
        currentPlayer.toDraw = 0
        endTurn()
    }
}