package common.commands

import common.steadfast
import common.util.onNull
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.http.ContentType.*
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.reply
import lib.model.ChannelId
import lib.model.channel.Embed
import lib.model.channel.EmbedImage
import lib.model.channel.Message
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.createMessage
import lib.rest.http.httpRequests.request

object LowLevelCommand : MessageCommand(State.All) {
    override val name: String = "ll"

    override val description: String = "raw REST requests"

    override val usage: String = "ll METHOD endpoint content"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        if (message.author != steadfast) {
            message.reply("Only Andrew is that cool")
        } else {
            println(message.args)
            with(message.args.iterator()) {
                if (!hasNext()) {
                    message.reply("HTTP method required")
                    return@with
                }
                val method = HttpMethod(next().toUpperCase())
                        .takeIf { it in HttpMethod.DefaultMethods }
                        .onNull { message.reply("Bad HTTP method") }
                        ?: return@with

                if (!hasNext()) {
                    message.reply("endpoint required")
                    return@with
                }
                val endpoint = next()
                println("endpoint = $endpoint")

                val content = message.args.drop(2).joinToString(separator = " ")
                val postMessageId = "/channels/(\\d+)/messages".toRegex().find(endpoint)
                if (method == HttpMethod.Post && postMessageId != null) {
                    val id = ChannelId(postMessageId.groupValues[1])
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
                    createMessage(id, cm)
                } else {
                    val response = request(
                            endpoint,
                            method,
                            TextContent(content, Application.Json)
                    )
                    println(response.status.description)
                    if (response.status.hashCode() == 200) {
                        response.readText().chunkedSequence(2000)
                                .forEach { message.reply(it) }
                    } else {
                        message.reply(response.status.description)
                    }
                }
            }
        }
    }
}