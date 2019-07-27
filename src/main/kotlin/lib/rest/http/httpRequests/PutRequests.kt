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
import lib.rest.updateRateLimitInfo

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
private suspend fun Bot.putRequest(url: String/*, jsonBody: String*/): HttpResponse {
    rateLimit()

    return client.put<HttpResponse>(api + url) {
        authHeaders.forEach { (k, v) ->
            header(k, v)
        }
        body = TextContent("", Application.Json)
    }.also(::updateRateLimitInfo)
}

/**
 * Create a reaction for the message.
 * `emoji` takes the form of `name:id` for custom guild emoji, or Unicode characters.
 * This endpoint requires the `READ_MESSAGE_HISTORY` permission to be present on the current user.
 * Additionally, if nobody else has reacted to the message using this emoji, this endpoint requires the `ADD_REACTIONS` permission to be present on the current user.
 * see also [https://discordapp.com/developers/docs/resources/channel#create-reaction]
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.createReaction(channelId: Snowflake, messageId: Snowflake, emoji: Char) {
    putRequest("/channels/$channelId/messages/$messageId/reactions/$emoji/@me")
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.addPin(channelId: Snowflake, messageId: Snowflake) {
    putRequest("/channels/$channelId/pins/$messageId")
}