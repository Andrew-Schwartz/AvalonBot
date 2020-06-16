package common.commands

import common.steadfast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

// TODO map by channel
var debug = false

object DebugCommand : MessageCommand(State.All) {
    override val name: String = "debug"

    override val description: String = "Enables debug mode for games. Only Andrew can do this"

    override val usage: String = "debug [true/false] [channel/dm id]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        if (message.author == steadfast) {
            when (message.args.firstOrNull()?.toLowerCase()) {
                null -> debug = !debug
                "true", "on" -> debug = true
                "false", "off" -> debug = false
            }
            message.reply("Debug mode is now ${if (debug) "on" else "off"}")
        } else {
            message.reply("Only Andrew is this cool")
        }
    }
}