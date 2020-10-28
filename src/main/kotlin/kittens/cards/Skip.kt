package kittens.cards

import io.ktor.util.*
import kittens.game.ExplodingKittens
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