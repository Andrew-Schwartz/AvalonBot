package avalonBot.commands

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message

object StartCommand : Command {
    override val name: String
        get() = "start"

    override val description: String
        get() = "if all players are ready, the game will start"

    override val usage: String
        get() = """!start ["now"]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}