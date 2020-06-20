package common.commands

import common.steadfast
import common.util.debug
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.ChannelId
import lib.model.channel.Message

//object Debug {
//    operator fun get(channel: IntoId<ChannelId>): Boolean {
//        return debug.getOrDefault(channel.intoId(), false)
//    }
//
//    operator fun set(channel: IntoId<ChannelId>, debug: Boolean) {
//        this.debug[channel.intoId()] = debug
//    }
//
//    override fun toString(): String {
//        "Debug = $debug"
//    }
//}

object DebugCommand : MessageCommand(State.All) {
    val debug = mutableMapOf<ChannelId, Boolean>()

    override val name: String = "debug"

    override val description: String = "Enables debug mode for games. Only Andrew can do this"

    override val usage: String = "debug [true/false] [channel/dm id]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        if (message.author == steadfast) {
            when (message.args.firstOrNull()?.toLowerCase()) {
                null -> message.channelId.debug = !message.channelId.debug
                "true", "on" -> message.channelId.debug = true
                "false", "off" -> message.channelId.debug = false
            }
            message.reply("Debug mode is now ${if (message.channelId.debug) "on" else "off"}")
        } else {
            message.reply("Only Andrew is this cool")
        }
    }
}