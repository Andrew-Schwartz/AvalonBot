package avalon.commands.game

import avalon.game.AvalonPlayer
import avalon.game.AvalonState
import common.commands.MessageCommand
import common.commands.State
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.send
import lib.model.channel.Message

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class LadyCommand(state: AvalonState, setter: (AvalonPlayer) -> Unit) : MessageCommand(State.Avalon.Lady) {
    override val name: String = "lotl"

    override val description: String = "Use to see someone's true loyalty (lotl is short for Lady of the Lake)"

    override val usage: String = "lotl <@player>"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        with(state) {
            if (message.author != ladyOfTheLake?.user) return@with

            when (val potentialTarget = message.mentions.firstOrNull()?.let { userPlayerMap[it] }) {
                null -> message.channel().send("No user given")
                ladyOfTheLake -> message.channel().send("You can't use the Lady of the Lake to determine your own loyalty")
                in pastLadies -> message.channel().send("${potentialTarget.user.username} has already had the Lady of the Lake")
                else -> setter(potentialTarget)
            }
        }
    }
}