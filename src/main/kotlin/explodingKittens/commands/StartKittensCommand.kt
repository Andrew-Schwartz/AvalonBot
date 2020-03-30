package explodingKittens.commands

import common.commands.Command
import common.commands.CommandState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

object StartKittensCommand : Command(CommandState.Setup) {
    override val name: String = "kittenstart"

    override val description: String = "if all players are ready, the game will start"

    override val usage: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}