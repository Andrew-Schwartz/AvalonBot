@file:Suppress("NAME_SHADOWING")

package lib.rest.http.httpRequests

import com.google.gson.JsonElement
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.HttpResponse
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.ChannelId
import lib.model.IntoId
import lib.model.UserId
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.http.CreateMessage
import lib.rest.model.events.receiveEvents.TypingStart
import lib.util.fromJson
import lib.util.j
import lib.util.toJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun postRequest(url: String, routeKey: String, jsonBody: String = "", typingChannel: IntoId<ChannelId>? = null): HttpResponse =
        request(routeKey, url, HttpMethod.Post, TextContent(jsonBody, Application.Json), typingChannel)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend inline fun postRequest(url: String, routeKey: String, json: JsonElement, typingChannel: IntoId<ChannelId>? = null) = postRequest(url, routeKey, json.toJson(), typingChannel)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun postFormDataRequest(url: String, routeKey: String, typingChannel: IntoId<ChannelId>? = null, formData: FormBuilder.() -> Unit): HttpResponse =
        request(routeKey, url, HttpMethod.Post, MultiPartFormDataContent(formData { formData() }), typingChannel)

/**
 * Post a message to a guild text or DM channel.
 * If operating on a guild channel, this endpoint requires the `SEND_MESSAGES` permission to be present on the current user.
 * If [CreateMessage.tts] is set to true, the `SEND_TTS_MESSAGES` permission is required for the message to be spoken.
 * Fires a Message Create Gateway event. See message formatting for more information on how to properly format messages.
 * @return the [Message] that was sent
 * @see [https://discordapp.com/developers/docs/resources/channel#create-message]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun createMessage(channelId: IntoId<ChannelId>, createMessage: CreateMessage): Message {
    val (content, _, _, files, embed) = createMessage
    val channelId = channelId.intoId();
    val url = "/channels/${channelId}/messages"

    val response = if (files == null) {
        postRequest(url, "POST-createMessage-${channelId}", createMessage.toJson(), channelId)
    } else {
        postFormDataRequest(url, "POST-createMessage-${channelId}", channelId) {
            if (content.isNotEmpty() || embed != null) {
                val payload = createMessage.copy(file = null).toJson()
                append("payload_json", payload)
            }

            files.forEach { (name, file) ->
                val bytes: ByteArray = file.readAllBytes()
                append("file", name, ContentType.MultiPart.FormData, bytes.size.toLong()) {
                    bytes.forEach(::writeByte)
                }
            }
        }
    }

    return response.fromJson<Message>().let(Bot.messages::addOrUpdate)
}

/**
 * Delete multiple messages in a single request.
 * This endpoint can only be used on guild channels and requires the `MANAGE_MESSAGES` permission.
 * Fires multiple Message Delete Gateway events.
 * Any message IDs given that do not exist or are invalid will count towards the minimum and maximum message count (currently 2 and 100 respectively).
 * Additionally, duplicated IDs will only be counted once.
 * see also [https://discordapp.com/developers/docs/resources/channel#bulk-delete-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun bulkDeleteMessages(channelId: IntoId<ChannelId>, messages: Set<Message>) {
    if (messages.size < 2) throw IllegalArgumentException("At least 2 messages are needed for bulk deletion")

    val channelId = channelId.intoId()
    val array: Array<Message> = messages.take(100).toTypedArray()
    postRequest("/channels/$channelId/messages/bulk-delete", "POST-bulkDeleteMessages-$channelId", j { "messages" to array })
}

/**
 * Post a typing indicator for the specified channel.
 * Generally bots should **not** implement this route.
 * However, if a bot is responding to a command and expects the computation to take a few seconds,
 * this endpoint may be called to let the user know that the bot is processing their message.
 * Fires a [TypingStart] Gateway event.
 * see also [https://discordapp.com/developers/docs/resources/channel#trigger-typing-indicator]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun triggerTypingIndicator(channelId: IntoId<ChannelId>) {
    val channelId = channelId.intoId()
    postRequest("/channels/$channelId/typing", "POST-triggerTypingIndicator-$channelId")
}

/**
 * See [https://discordapp.com/developers/docs/resources/user#create-dm]
 * Create a new DM channel with a user.
 * @param userId the recipient to open a DM channel with
 * @return DM [Channel] object.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun createDM(userId: IntoId<UserId>): Channel {
    val userId = userId.intoId()
    return Bot.channels.computeIfAbsent(userId) {
        postRequest("/users/@me/channels", "POST-createDM", j { "recipient_id" to "$userId" }).fromJson()
    }
}
