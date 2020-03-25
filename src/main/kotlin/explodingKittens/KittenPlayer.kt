package explodingKittens

import explodingKittens.cards.Card
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User
import main.game.Player

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenPlayer(name: String, user: User) : Player(name, user) {
    val hand = arrayListOf<Card>()

    var toDraw = 1
}