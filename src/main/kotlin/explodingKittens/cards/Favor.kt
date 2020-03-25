package explodingKittens.cards

import explodingKittens.KittenState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.blockUntil
import lib.dsl.off
import lib.dsl.on
import lib.model.user.User
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.util.ping
import main.util.MessageListener
import main.util.cards
import main.util.contains
import main.util.one

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Favor(id: Int) : Card(id) {
    override val description: String = "A player of your choice gives you a card of their choice from their hand"

    override suspend fun KittenState.play() {
        with(game.bot) {
            game.channel.send("${currentPlayer.user.ping()}, choose another player")

            // block until they @ someone in same channel
            var target: User? = null

            val getTarget: MessageListener = {
                game.bot.run {
                    if (author != currentPlayer.user) return@run
                    if (channel != game.channel) return@run
                    if (mentions.one { it in players.map { it.user } })
                        target = mentions.first { it in players.map { it.user } }
                }
            }
            on(MessageCreate, λ = getTarget)
            blockUntil { target != null }
            off(MessageCreate, λ = getTarget)

            // that player chooses one of their cards (secretly)
            target!!.sendDM("Name a card in your hand to give up")

            var card: Card? = null
            val getCard: MessageListener = {
                game.bot.run {
                    if (channel != target!!.getDM()) return@run

                    val cards = args.cards()
                    if (cards.size != 1) return@run
                    val hand = userPlayerMap.getValue(target!!).hand
                    if (cards[0] !in hand) return@run
                    card = hand.first { it::class == cards[0] }
                }
            }
            on(MessageCreate, λ = getCard)
            blockUntil { card != null }
            off(MessageCreate, λ = getCard)

            // give first player that card and take it from second player
            currentPlayer.hand -= card!!
            userPlayerMap.getValue(target!!).hand += card!!
        }
    }
}