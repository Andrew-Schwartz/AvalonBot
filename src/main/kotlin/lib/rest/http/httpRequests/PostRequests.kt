package lib.rest.http.httpRequests

import com.google.gson.JsonElement
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Snowflake
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.api
import lib.rest.client
import lib.rest.http.CreateMessage
import lib.rest.rateLimit
import lib.rest.updateRateLimitInfo
import lib.util.J
import lib.util.fromJson
import lib.util.toJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.postRequest(url: String, jsonBody: String): HttpResponse {
    rateLimit()

    return client.post<HttpResponse>(api + url) {
        authHeaders.forEach { (key, value) ->
            header(key, value)
        }
        body = TextContent(jsonBody, Application.Json)
    }.also(::updateRateLimitInfo)
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
private suspend inline fun Bot.postRequest(url: String, json: JsonElement) = postRequest(url, json.toJson())

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.postFormDataRequest(url: String, formData: FormBuilder.() -> Unit): HttpResponse {
    rateLimit()

    return client.post<HttpResponse>(api + url) {
        authHeaders.forEach { (key, value) ->
            header(key, value)
        }

        body = MultiPartFormDataContent(formData {
            formData()
        })
    }.also(::updateRateLimitInfo)
}

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
suspend fun Bot.createMessage(channel: Channel, createMessage: CreateMessage): Message {
    val (content, _, _, files, embed) = createMessage
    val url = "/channels/${channel.id}/messages"

    val response = if (files == null) {
        postRequest(url, createMessage.toJson())
    } else {
        postFormDataRequest(url) {
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

    return response.fromJson<Message>().also(messages::add)
}

/**
 * See [https://discordapp.com/developers/docs/resources/user#create-dm]
 * Create a new DM channel with a user.
 * @param userId the recipient to open a DM channel with
 * @return DM [Channel] object.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.createDM(userId: Snowflake): Channel = channels.computeIfAbsent(userId) {
    postRequest("/users/@me/channels", J("recipient_id", userId.value)).fromJson()
}
