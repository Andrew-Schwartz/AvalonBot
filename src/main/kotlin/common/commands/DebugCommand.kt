package common.commands

import common.steadfast
import common.util.debug
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.send
import lib.model.ChannelId
import lib.model.channel.Message

object DebugCommand : MessageCommand(State.All) {
    val debug = mutableMapOf<ChannelId, Boolean>()

    override val name: String = "debug"

    override val description: String = "Enables debug mode for games. Only Andrew can do this"

    override val usage: String = "debug [true/false] [channel/dm id]"

    override val privileged: Boolean = true

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        if (message.author == steadfast) {
            when (message.args.firstOrNull()?.toLowerCase()) {
                null -> message.channelId.debug = !message.channelId.debug
                "true", "on" -> message.channelId.debug = true
                "false", "off" -> message.channelId.debug = false
            }
            message.channel().send("Debug mode is now ${if (message.channelId.debug) "on" else "off"}")
        } else {
            message.channel().send("Only Andrew is this cool")
        }
    }
}