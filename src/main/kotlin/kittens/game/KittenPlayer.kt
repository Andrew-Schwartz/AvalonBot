package kittens.game

import common.game.Player
import io.ktor.util.*
import kittens.cards.Card
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.guild.Guild
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenPlayer(user: User, guild: Guild?) : Player(user, guild) {
    val hand = arrayListOf<Card>()

    var numTurns = 1

    override fun reset() {
        hand.clear()
        numTurns = 1
    }
}