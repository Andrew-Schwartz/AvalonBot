package lib.rest.http.httpRequests

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.misc.fromJson
import lib.model.*
import lib.rest.api
import lib.rest.authHeaders
import lib.rest.client
import lib.rest.http.GetChannelMessages
import lib.rest.model.BotGateway

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
private suspend fun Bot.getRequest(url: String): HttpResponse {
    return client.get(api + url) {
        authHeaders(token).forEach { (key, value) ->
            header(key, value)
        }
    }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.getUser(id: Snowflake = "@me".snowflake()): User {
    return getRequest("/users/$id").fromJson()
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getChannel(id: Snowflake): Channel {
    return getRequest("/channels/$id").fromJson()
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getMessage(channelId: Snowflake, messageId: Snowflake): Message {
    return getRequest("/channels/$channelId/messages/$messageId").fromJson()
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.getMessages(getChannelMessages: GetChannelMessages): Array<Message> {
    return getRequest("/channels/${getChannelMessages.channel}/messages?${getChannelMessages.queryParams}").fromJson()
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.gateway(): String {
    return getRequest("/gateway/bot").fromJson<BotGateway>().url.removePrefix("wss://")
}

