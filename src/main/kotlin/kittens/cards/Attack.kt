package kittens.cards

import io.ktor.util.*
import kittens.game.ExplodingKittens
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Attack(id: Int) : Card(id) {
    override val description: String = "End your turn without drawing a card and force the next player to take two turns in a row"

    override suspend fun ExplodingKittens.play() {
//        nextPlayer.toDraw += currentPlayer.toDraw
        state.nextPlayer.numTurns = 2
        state.currentPlayer.numTurns = 0
        endTurn()
    }
}