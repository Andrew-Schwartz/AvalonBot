package explodingKittens.commands

import common.commands.Command
import common.commands.CommandState.KittensGame
import explodingKittens.KittenState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class HandCommand(private val state: KittenState) : Command(KittensGame) {
    override val name = "hand"

    override val description = "Lists the cards in your hand"

    override val usage = "!hand"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        with(state) {
            message.author.sendDM(
                    userPlayerMap[message.author]?.hand
                            ?.sortedBy { it.name }
                            ?.joinToString(separator = "\n") { it.name }
                            ?: "You aren't in this game of exploding kittens, so you have no cards in your hand"
            )
        }
    }
}