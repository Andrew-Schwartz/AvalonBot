package explodingKittens

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import main.bot
import main.game.Game
import main.game.GameType
import main.game.Setup

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class ExplodingKittens(setup: Setup) : Game(GameType.ExplodingKittens, setup) {
    val state: KittenState = KittenState(this, setup)

    override suspend fun startGame(): Unit = bot.run {
        with(state) {

        }
    }

    suspend fun drawCard() {
        val card = state.deck.removeAt(0)
        card.run { draw() }
    }

    fun endTurn() {
        // todo add chat display saying whose turn it is
        if (--state.currentPlayer.numTurns == 0) {
            state.currentPlayerIndex = next(state.currentPlayerIndex)
        }
    }

    fun next(index: Int): Int {
        var index = index
        index += state.turnOrder
        index %= state.players.size
        if (index < 0)
            index += 5
        return index
    }
}