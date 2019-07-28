package lib.rest.http.httpRequests

import com.google.gson.JsonElement
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
import lib.rest.http.EditChannelPermissions
import lib.rest.rateLimit
import lib.rest.updateRateLimitInfo
import lib.util.toJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.putRequest(url: String, jsonBody: String = ""): HttpResponse {
    rateLimit()

    return client.put<HttpResponse>(api + url) {
        authHeaders.forEach { (k, v) ->
            header(k, v)
        }
        body = TextContent(jsonBody, Application.Json)
    }.also(::updateRateLimitInfo)
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend inline fun Bot.putRequest(url: String, json: JsonElement) = putRequest(url, json.toJson())

/**
 * Create a reaction for the message.
 * `emoji` takes the form of `name:id` for custom guild emoji, or Unicode characters.
 * This endpoint requires the `READ_MESSAGE_HISTORY` permission to be present on the current user.
 * Additionally, if nobody else has reacted to the message using this emoji, this endpoint requires the `ADD_REACTIONS` permission to be present on the current user.
 * see also [https://discordapp.com/developers/docs/resources/channel#create-reaction]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.createReaction(channelId: Snowflake, messageId: Snowflake, emoji: Char) {
    putRequest("/channels/$channelId/messages/$messageId/reactions/$emoji/@me")
}

/**
 * Edit the channel permission overwrites for a user or role in a channel.
 * Only usable for guild channels. Requires the `MANAGE_ROLES` permission.
 * see also [https://discordapp.com/developers/docs/resources/channel#edit-channel-permissions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.editChannelPermissions(channelId: Snowflake, overwriteId: Snowflake, editChannelPermissions: EditChannelPermissions) {
    getChannel(channelId).guildId ?: throw IllegalArgumentException("Only usable for guild channels")

    putRequest("/channels/$channelId/permissions/$overwriteId", editChannelPermissions.toJson())
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.addPin(channelId: Snowflake, messageId: Snowflake) {
    putRequest("/channels/$channelId/pins/$messageId")
}