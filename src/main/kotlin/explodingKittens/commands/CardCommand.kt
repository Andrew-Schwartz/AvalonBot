package explodingKittens.commands

import explodingKittens.KittenState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.inlineCode
import main.commands.Command
import main.commands.CommandState.KittensGame
import main.util.cards
import kotlin.reflect.full.primaryConstructor

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class CardCommand(private val state: KittenState) : Command(KittensGame) {
    override val name: String = "card"

    override val description: String = "Gives information about what specific Exploding Kittens cards do"

    override val usage: String = "card <card1> [card2] [card3]..."

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        with(state) {
            val cardClasses = args.cards().distinct()

            val player = userPlayerMap[message.author]

            when (cardClasses.size) {
                0 -> message.author.sendDM("No cards specified. Say ${"!help card".inlineCode()} for help")
                1 -> message.author.sendDM {
                    val cardClass = cardClasses.first()
                    val card = player?.hand?.firstOrNull { it::class == cardClass }
                            ?: cardClass.primaryConstructor!!.call(0)
                    title = card.name
                    description = card.description
                    image(card.image)
                }
                else -> message.author.sendDM {
                    title = "Card help"

                    cardClasses.map { it.primaryConstructor!!.call(0) }
                            .forEach {
                                addField(it.name, it.description)
                            }

                    // add each card as field
                }
            }
        }
    }
}