package kittens.commands

import common.commands.MessageCommand
import common.commands.State
import common.game.Game
import common.game.GameType
import common.util.cards
import io.ktor.util.KtorExperimentalAPI
import kittens.game.ExplodingKittens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.inlineCode
import kotlin.reflect.full.primaryConstructor

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
object CardCommand : MessageCommand(State.Kittens.Game) {
    override val name: String = "card"

    override val description: String = "Gives information about what specific Exploding Kittens cards do"

    override val usage: String = "card <card1> [card2] [card3]..."

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val state = (Game[message.channel(), GameType.Kittens] as ExplodingKittens).state
        with(state) {
            val cardClasses = message.args.cards().distinct()

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