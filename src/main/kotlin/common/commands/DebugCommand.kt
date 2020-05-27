package common.commands

import common.steadfast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

var debug = false

object DebugCommand : Command(State.Setup) {
    override val name: String = "debug"

    override val description: String = "Enables debug mode for games. Only Andrew can do this"

    override val usage: String = "debug [true/false]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        if (message.author == steadfast) {
            when (args.firstOrNull()) {
                null -> debug = !debug
                "true" -> debug = true
                "false" -> debug = false
            }
            message.reply("Debug mode is now ${if (debug) "on" else "off"}")
        } else {
            message.reply("Only Andrew is this cool")
        }
    }
}