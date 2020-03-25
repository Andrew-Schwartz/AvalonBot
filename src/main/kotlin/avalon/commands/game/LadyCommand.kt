package avalon.commands.game

import avalon.game.Avalon
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import main.commands.Command
import main.commands.CommandState.AvalonGame

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class LadyCommand(private val state: Avalon) : Command(AvalonGame) {
    override val name: String = "lady"

    override val description: String = "Use to see someone's true loyalty"

    override val usage: String = "!lady <player>"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        with(message) {
            if (author != state.ladyOfTheLake?.user) return@with

            val name = content.substringAfter(' ')

            when (val potentialTarget = state.playerByName(name)) {
                null -> reply("No user found for the name $name")
                state.ladyOfTheLake -> reply("You can't use the Lady of the Lake to determine your own loyalty")
                in state.pastLadies -> reply("$name has already had the Lady of the Lake")
                else -> state.ladyTarget = potentialTarget
            }
        }
    }
}