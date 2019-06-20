package lib.rest.http.httpRequests

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application
import io.ktor.http.ContentType.MultiPart
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
private suspend fun Bot.postRequest(url: String, jsonBody: String, contentType: ContentType = Application.Json): HttpResponse {
    return client.post(api + url) {
        authHeaders.forEach { (key, value) ->
            header(key, value)
        }
        body = TextContent(jsonBody, contentType)
//        body = TextContent(jsonBody, MultiPart.FormData)
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

/**
 * post a message in the channel via multipart/form-data
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.createMessage(channel: Channel, createMessage: CreateMessage): Message {
    val response = postRequest("/channels/${channel.id.value}/messages", createMessage.toJson(), MultiPart.FormData)
    val message: Message = response.fromJson()
    messages += message
    return message
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.sendImage(channel: Channel, createMessage: CreateMessage) {
    val response = client.post<HttpResponse>("$api/channels/${channel.id}/messages") {
        val (content, _, _, file, embed) = createMessage

        for ((key, value) in authHeaders) {
            header(key, value)
        }

//        if (file.isEmpty()) {
//            body = TextContent(createMessage.toJson(), Application.Json)
//        } else {
        body = MultiPartFormDataContent(formData {
            if (content.isNotEmpty() || embed != null) {
                val payload = createMessage.copy(payloadJson = null, file = null).toJson()

                append("payload_json", payload)
//                append("file", file)
            }
        })
//        }

    }
    println("response: ${response.readText()}")
}