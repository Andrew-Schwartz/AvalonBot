package explodingKittens

import explodingKittens.cards.Card
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User
import main.game.Player

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenPlayer(user: User) : Player(user) {
    val hand = arrayListOf<Card>()

    var numTurns = 1
}