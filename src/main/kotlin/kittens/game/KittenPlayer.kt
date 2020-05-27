package kittens.game

import common.game.Player
import io.ktor.util.KtorExperimentalAPI
import kittens.cards.Card
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenPlayer(user: User) : Player(user) {
    val hand = arrayListOf<Card>()

    var numTurns = 1
}