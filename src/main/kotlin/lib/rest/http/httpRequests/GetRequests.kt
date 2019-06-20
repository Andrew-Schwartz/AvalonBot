package lib.rest.http.httpRequests

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Channel
import lib.model.Message
import lib.model.Snowflake
import lib.model.User
import lib.rest.api
import lib.rest.client
import lib.rest.http.GetChannelMessages
import lib.rest.model.BotGateway
import lib.util.fromJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.getRequest(url: String): HttpResponse {
    return client.get(api + url) {
        authHeaders.forEach { (key, value) ->
            header(key, value)
        }
    }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.getUser(id: Snowflake): User {
    return users.getOrPut(id) { getRequest("/users/$id").fromJson() }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getChannel(id: Snowflake): Channel {
    return channels.getOrPut(id) { getRequest("/channels/$id").fromJson() }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getMessage(channelId: Snowflake, messageId: Snowflake): Message {
    return messages.getOrPut(messageId) { getRequest("/channels/$channelId/messages/$messageId").fromJson() }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.getMessages(getChannelMessages: GetChannelMessages): Array<Message> {
    return getRequest("/channels/${getChannelMessages.channel}/messages?${getChannelMessages.queryParams}").fromJson<Array<Message>>().also {
        for (message in it) messages.putIfAbsent(message)
    }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.gateway(): String {
    return getRequest("/gateway/bot").fromJson<BotGateway>().url.removePrefix("wss://")
}

