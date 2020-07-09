package avalon.commands.game

import avalon.game.Avalon
import common.commands.MessageCommand
import common.commands.State
import common.game.Game
import common.game.GameType
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.reply
import lib.model.channel.Message

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
object LadyCommand : MessageCommand(State.Avalon.Lady) {
    override val name: String = "lotl"

    override val description: String = "Use to see someone's true loyalty (lotl is short for Lady of the Lake)"

    override val usage: String = "lotl <@player>"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val state = (Game[message.channel(), GameType.Avalon] as Avalon).state

        with(message) {
            if (author != state.ladyOfTheLake?.user) return@with

            when (val potentialTarget = message.mentions.firstOrNull()?.let { state.userPlayerMap[it] }) {
                null -> reply("No user given")
                state.ladyOfTheLake -> reply("You can't use the Lady of the Lake to determine your own loyalty")
                in state.pastLadies -> reply("${potentialTarget.user.username} has already had the Lady of the Lake")
                else -> state.ladyTarget = potentialTarget
            }
        }
    }
}