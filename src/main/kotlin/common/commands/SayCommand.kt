package common.commands

import common.steadfast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.ChannelId
import lib.model.channel.Embed
import lib.model.channel.EmbedImage
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
                val content = message.args.drop(1).joinToString(separator = " ")
                val cm = when {
                    message.embeds.isNotEmpty() -> {
                        val embed = message.embeds.first()
                        CreateMessage(
                                content = content,
                                embed = embed,
                                file = embed.files
                        )
                    }
                    message.attachments.isNotEmpty() -> {
                        val attachment = message.attachments.first()
                        CreateMessage(
                                content = content,
                                embed = Embed(image = EmbedImage(
                                        attachment.url,
                                        attachment.proxyUrl,
                                        attachment.height,
                                        attachment.width
                                )))
                    }
                    else -> {
                        CreateMessage(content = content)
                    }
                }
                createMessage(ChannelId(channelId), cm)
            }
        }
    }
}