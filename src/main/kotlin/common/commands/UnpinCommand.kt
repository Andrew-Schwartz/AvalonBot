package common.commands

import common.util.L
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.*
import lib.model.MessageId
import lib.model.channel.Message
import lib.rest.http.httpRequests.getPinnedMessages

object UnpinCommand : MessageCommand(State.All) {
    override val name: String = "unpin"

    override val description: String = "Unpins all message in this channel, or the newest/oldest x. Oldest is default"

    override val usage: String = "unpin [new/old #]"

    override val privileged: Boolean = true

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val args = message.args
        if (args.isEmpty()) {
            getPinnedMessages(message.channelId).forEach { it.unpin() }
            message.react('✅')
        } else {
            when (args.size) {
                1 -> {
                    val arg = args.single()
                    val pins = runCatching {
                        L[message.channel().getMessage(MessageId(arg))]
                    }.getOrElse {
                        arg.toIntOrNull()?.let { num ->
                            getPinnedMessages(message.channelId)
                                    .sortedBy { it.timestamp }
                                    .take(num)
                        } ?: emptyList()
                    }
                    if (pins.isEmpty()) {
                        message.reply("How many pins to remove?")
                    } else {
                        pins.forEach { it.unpin() }
                        message.react('✅')
                    }
                }
                2 -> args.last().toIntOrNull()?.let { num ->
                    val pins = getPinnedMessages(message.channelId).sortedBy { it.timestamp }
                    when (args.first().toLowerCase()) {
                        "new" -> pins.takeLast(num)
                        "old" -> pins.take(num)
                        else -> {
                            message.reply("Unknown args. Use `!help unpin` to find out more")
                            listOf()
                        }
                    }.forEach { it.unpin() }
                    message.react('✅')
                } ?: message.reply("How many pins to remove?")
                else -> message.reply("Unknown args. Use `!help unpin` to find out more")
            }
        }
    }
}