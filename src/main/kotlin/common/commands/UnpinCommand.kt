package common.commands

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.rest.http.httpRequests.getPinnedMessages

object UnpinCommand : MessageCommand(State.All) {
    override val name: String = "unpin"

    override val description: String = "Unpins all message in this channel, or the newest/oldest x. Oldest is default"

    override val usage: String = "unpin [new/old #]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val args = message.args
        if (args.isEmpty()) {
            getPinnedMessages(message.channelId).forEach { it.unpin() }
        } else {
            when (args.size) {
                1 -> args.single().toIntOrNull()?.let { num ->
                    getPinnedMessages(message.channelId)
                            .sortedBy { it.timestamp }
                            .take(num)
                            .forEach { it.unpin() }
                    message.react('✅')
                } ?: message.reply("How many pins to remove?")
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