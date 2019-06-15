package lib.rest.http.httpRequests

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.content.TextContent
import io.ktor.http.ContentType.Application
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.misc.fromJson
import lib.misc.toJson
import lib.model.Channel
import lib.model.Message
import lib.rest.api
import lib.rest.authHeaders
import lib.rest.client
import lib.rest.http.CreateDM
import lib.rest.http.CreateMessage

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.postRequest(url: String, jsonBody: String): HttpResponse {
    return client.post(api + url) {
        authHeaders(token).forEach { (key, value) ->
            header(key, value)
        }
        body = TextContent(jsonBody, Application.Json)
    }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.createDM(createDM: CreateDM): Channel {
    return postRequest("/users/@me/channels", createDM.toJson()).fromJson<Channel>().also { channels[it.id] = it }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.createMessage(channel: Channel, createMessage: CreateMessage): Message {
    return postRequest("/channels/${channel.id.value}/messages", createMessage.toJson()).fromJson<Message>().also { messages[it.id] = it }
}