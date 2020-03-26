package explodingKittens

import common.game.Player
import explodingKittens.cards.Card
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenPlayer(user: User) : Player(user) {
    val hand = arrayListOf<Card>()

    var numTurns = 1
}