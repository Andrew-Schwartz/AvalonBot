package kittens.game

import common.game.Game
import common.game.GameFinish
import common.game.GameType
import common.game.Setup
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class ExplodingKittens(setup: Setup) : Game(GameType.Kittens, setup) {
    override val state = KittenState(setup)

    override suspend fun runGame(): GameFinish = with(state) game@{
        TODO()
    }

    suspend fun drawCard() {
        val card = state.deck.removeAt(0)
        card.run { draw() }
    }

    fun endTurn() {
        // todo add chat display saying whose turn it is
        if (--state.currentPlayer.numTurns == 0) {
            state.currentPlayerIndex = state.next(state.currentPlayerIndex)
        }
    }
}