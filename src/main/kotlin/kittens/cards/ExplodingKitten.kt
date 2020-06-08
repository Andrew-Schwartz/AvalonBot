package kittens.cards

import common.bot
import common.game.name
import io.ktor.util.KtorExperimentalAPI
import kittens.game.ExplodingKittens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.embed
import lib.dsl.suspendUntil
import lib.util.bold

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class ExplodingKitten(id: Int) : Card(id) {
    override val description: String = "Explodes when drawn unless a ${"Defuse".bold()} card is used"

    override val playable = false

    override suspend fun ExplodingKittens.draw() {
        val player = state.currentPlayer
        val embed = embed {
            title = "Exploding Kitten!".bold()
            image(image)
        }
        val defuse = player.hand.firstOrNull { it::class == Defuse::class }

        if (defuse != null) {
            embed.description = "${player.name} defuses the kitten and places it into the deck!"
            player.hand.remove(defuse)
        } else {
            embed.description = "${player.name} cannot defuse the kitten and blows up!"
            state.players.remove(player)
            state.currentPlayerIndex-- // todo I think this is needed
        }
        with(bot) {
            channel.send(embed = embed)
            if (defuse != null) {
                player.user.sendDM("""Where will you put the exploding kitten?
                                     |Reply how far from the top to put it (0 being the topmost card)
                                     |or use a negative number to count from the bottom (-0 is bottom card)
                                     |There are currently ${state.deck.size} cards in the deck
                """.trimMargin())

                var bottom = true
                var num = 0

                suspendUntil {
                    val message = player.user.getDM().lastMessage
                    if (message.author != player.user) return@suspendUntil false
                    message.content.trim()
                            .toIntOrNull()
                            ?.let {
                                num = it
                                bottom = message.content.startsWith('-')
                                true
                            }
                            ?: false
                }

                val index = if (bottom) state.deck.size - num else num
                state.deck.add(index, this@ExplodingKitten)
            }
        }
    }
}