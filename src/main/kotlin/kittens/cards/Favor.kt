package kittens.cards

import common.bot
import common.util.MessageListener
import common.util.cards
import common.util.contains
import kittens.game.ExplodingKittens
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.blockUntil
import lib.dsl.off
import lib.dsl.on
import lib.model.user.User
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.util.ping

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Favor(id: Int) : Card(id) {
    override val description: String = "A player of your choice gives you a card of their choice from their hand"

    override suspend fun ExplodingKittens.play() {
        with(bot) {
            channel.send("${state.currentPlayer.user.ping()}, choose another player")

            // block until they @ someone in same channel
            var target: User? = null

            val getTarget: MessageListener = {
                bot.run {
                    if (author != state.currentPlayer.user) return@run
                    if (channel != this@play.channel) return@run
                    if (mentions.filter { it in state.players.map { it.user } }.size == 1)
                        target = mentions.first { it in state.players.map { it.user } }
                }
            }
            on(MessageCreate, λ = getTarget)
            blockUntil { target != null }
            off(MessageCreate, λ = getTarget)

            // that player chooses one of their cards (secretly)
            target!!.sendDM("Name a card in your hand to give up")

            var card: Card? = null
            val getCard: MessageListener = {
                bot.run {
                    if (channel != target!!.getDM()) return@run

                    val cards = args.cards()
                    if (cards.size != 1) return@run
                    val hand = state.userPlayerMap.getValue(target!!).hand
                    if (cards[0] !in hand) return@run
                    card = hand.first { it::class == cards[0] }
                }
            }
            on(MessageCreate, λ = getCard)
            blockUntil { card != null }
            off(MessageCreate, λ = getCard)

            // give first player that card and take it from second player
            state.currentPlayer.hand -= card!!
            state.userPlayerMap.getValue(target!!).hand += card!!
        }
    }
}