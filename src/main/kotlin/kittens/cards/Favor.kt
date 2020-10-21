package kittens.cards

import common.util.cards
import common.util.contains
import io.ktor.util.*
import kittens.game.ExplodingKittens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.*
import lib.model.channel.Message
import lib.model.user.User
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.util.ping

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Favor(id: Int) : Card(id) {
    override val description: String = "A player of your choice gives you a card of their choice from their hand"

    override suspend fun ExplodingKittens.play() {
        channel.send("${state.currentPlayer.user.ping()}, choose another player")

        // block until they @ someone in same channel
        var target: User? = null

        val getTarget: suspend Message.() -> Unit = {
            if (author == state.currentPlayer.user && channel != this@play.channel) {
                if (mentions.filter { it in state.players.map { it.user } }.size == 1)
                    target = mentions.first { it in state.players.map { it.user } }
            }
        }
        // todo make this a command
        Bot.on(MessageCreate, λ = getTarget)
        suspendUntil { target != null }
        Bot.off(MessageCreate, λ = getTarget)

        // that player chooses one of their cards (secretly)
        target!!.sendDM("Name a card in your hand to give up")

        var card: Card? = null
        val getCard: suspend Message.() -> Unit = {
            if (channel == target!!.getDM()) {
                val cards = args.cards()
                if (cards.size == 1) {
                    val hand = state.userPlayerMap.getValue(target!!).hand
                    if (cards[0] in hand) {
                        card = hand.first { it::class == cards[0] }
                    }
                }
            }
        }
        // make this a command
        Bot.on(MessageCreate, λ = getCard)
        suspendUntil { card != null }
        Bot.off(MessageCreate, λ = getCard)

        // give first player that card and take it from second player
        state.currentPlayer.hand -= card!!
        state.userPlayerMap.getValue(target!!).hand += card!!
    }
}