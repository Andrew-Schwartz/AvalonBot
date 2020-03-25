package explodingKittens.cards

import explodingKittens.KittenState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.blockUntil
import lib.dsl.embed
import lib.util.bold

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class ExplodingKitten(id: Int) : Card(id) {
    override val description: String = "Explodes when drawn unless a ${"Defuse".bold()} card is used"

    override val playable = false

    override suspend fun KittenState.draw() {
        val player = currentPlayer
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
            game.gamePlayers.remove(player)
            currentPlayerIndex-- // todo I think this is needed
        }
        with(game.bot) {
            game.channel.send(embed = embed)
            if (defuse != null) {
                player.user.sendDM("""Where will you put the exploding kitten?
                                     |Reply how far from the top to put it (0 being the topmost card)
                                     |or use a negative number to count from the bottom (-0 is bottom card)
                                     |There are currently ${deck.size} cards in the deck
                """.trimMargin())

                var bottom = true
                var num = 0

                blockUntil {
                    val message = player.user.getDM().lastMessage
                    if (message.author != player.user) return@blockUntil false
                    message.content.trim()
                            .toIntOrNull()
                            ?.let {
                                num = it
                                bottom = message.content.startsWith('-')
                                true
                            }
                            ?: false
                }

                val index = if (bottom) deck.size - num else num
                deck.add(index, this@ExplodingKitten)
            }
        }
    }
}