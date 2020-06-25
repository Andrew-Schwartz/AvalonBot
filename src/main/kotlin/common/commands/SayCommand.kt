package common.commands

import common.steadfast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.ChannelId
import lib.model.channel.Message
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.createMessage

object SayCommand : MessageCommand(State.All) {
    override val name: String = "say"

    override val description: String = "Says something in some channel"

    override val usage: String = "say <channelId> <message/embed>"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        if (message.author != steadfast) {
            message.reply("Only Andrew is that cool")
        } else {
            message.args.firstOrNull()?.let { channelId ->
                createMessage(ChannelId(channelId), CreateMessage(
                        content = message.args.drop(1).joinToString(separator = " "),
                        embed = message.embeds.first(),
                        file = message.embeds.first().files
                ))
            }
        }
    }
}