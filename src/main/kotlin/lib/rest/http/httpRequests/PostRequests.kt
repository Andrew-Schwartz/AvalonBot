package lib.rest.http.httpRequests

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
import lib.model.Channel
import lib.model.Message
import lib.model.Snowflake
import lib.model.snowflake
import lib.rest.api
import lib.rest.client
import lib.rest.http.CreateDM
import lib.rest.http.CreateMessage
import lib.util.fromJson
import lib.util.toJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.postRequest(url: String, jsonBody: String): HttpResponse {
    return client.post(api + url) {
        authHeaders.forEach { (key, value) ->
            header(key, value)
        }
        body = TextContent(jsonBody, Application.Json)
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.postFormDataRequest(url: String, formData: FormBuilder.() -> Unit): HttpResponse {
    return client.post(api + url) {
        authHeaders.forEach { (key, value) ->
            header(key, value)
        }

        body = MultiPartFormDataContent(formData {
            formData()
        })
    }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.createDM(createDM: CreateDM): Channel {
    val id = createDM.userId.snowflake()
    return channels.getOrPut(id) {
        postRequest("/users/@me/channels", createDM.toJson()).fromJson()
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.createDM(userId: Snowflake): Channel = createDM(CreateDM(userId.value))

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
                val payload = createMessage.copy(payloadJson = null, file = null).toJson()
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

    val message: Message = response.fromJson()
    messages += message
    return message
}
