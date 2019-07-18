package lib.rest.http.httpRequests

import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.content.TextContent
import io.ktor.http.ContentType.Application
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Snowflake
import lib.rest.api
import lib.rest.client
import lib.rest.rateLimit
import lib.rest.useRateLimit

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
private suspend fun Bot.putRequest(url: String, jsonBody: String): HttpResponse {
    rateLimit()

    return client.put<HttpResponse>(api + url) {
        authHeaders.forEach { (k, v) ->
            header(k, v)
        }
        body = TextContent(jsonBody, Application.Json)
    }.also(::useRateLimit).also {
        it.headers.forEach { k, v -> println("$k: $v") }
    }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.createReaction(channelId: Snowflake, messageId: Snowflake, emoji: Char) {
    putRequest("/channels/$channelId/messages/$messageId/reactions/$emoji/@me", "")
}