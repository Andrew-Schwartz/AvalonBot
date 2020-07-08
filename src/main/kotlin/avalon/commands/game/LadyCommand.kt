package avalon.commands.game

import avalon.game.AvalonState
import common.commands.MessageCommand
import common.commands.State
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
object LadyCommand : MessageCommand(State.Avalon.Lady) {
    override val name: String = "lotl"

    override val description: String = "Use to see someone's true loyalty (lotl is short for Lady of the Lake)"

    override val usage: String = "lotl <@player>"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val state = AvalonState.inChannel(message.channel())

        with(message) {
            if (author != state?.ladyOfTheLake?.user) return@with

            when (val potentialTarget = message.mentions.firstOrNull()?.let { state.userPlayerMap[it] }) {
                null -> reply("No user given")
                state.ladyOfTheLake -> reply("You can't use the Lady of the Lake to determine your own loyalty")
                in state.pastLadies -> reply("${potentialTarget.user.username} has already had the Lady of the Lake")
                else -> state.ladyTarget = potentialTarget
            }
        }
    }
}