package lib.rest.http.httpRequests

import com.google.gson.JsonElement
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.response.HttpResponse
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.RichEmbed
import lib.exceptions.RequestException
import lib.model.Snowflake
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.api
import lib.rest.client
import lib.rest.http.ModifyChannelOptions
import lib.rest.model.events.receiveEvents.ChannelUpdate
import lib.rest.model.events.receiveEvents.MessageUpdate
import lib.util.fromJson
import lib.util.j
import lib.util.toJson

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
private suspend fun Bot.patchRequest(url: String, jsonBody: String): HttpResponse {
    return client.patch(api + url) {
        authHeaders.forEach { (k, v) ->
            header(k, v)
        }

        body = TextContent(jsonBody, ContentType.Application.Json)
    }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
private suspend inline fun Bot.patchRequest(url: String, json: JsonElement) = patchRequest(url, json.toJson())

/**
 * see [https://discordapp.com/developers/docs/resources/channel#modify-channel]
 * Update a channels settings. Requires the `MANAGE_CHANNELS` permission for the guild.
 * Fires a [ChannelUpdate] event.
 * If modifying a category, individual [ChannelUpdate] events will fire for each child channel that also changes.
 * @return a [Channel] on success
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.modifyChannel(channelId: Snowflake, modifyInfo: ModifyChannelOptions): Result<Channel> {
    val response = patchRequest("/channels/$channelId", (modifyInfo forChannel getChannel(channelId)).toJson())
    return when (response.status) {
        HttpStatusCode.BadRequest -> Result.failure(RequestException("400 Bad Request, Invalid parameters"))
        else -> Result.success(response.fromJson())
    }
}

/**
 * Edit a previously sent message.
 * You can only edit messages that have been sent by the current user.
 * Fires a [MessageUpdate] Gateway event.
 * @return the new [Message]
 * see also [https://discordapp.com/developers/docs/resources/channel#edit-message]
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.editMessage(channelId: Snowflake, messageId: Snowflake, content: String? = null, embed: RichEmbed? = null): Message {
    return patchRequest("/channels/$channelId/messages/$messageId", j {
        if (content != null) add("content", content.take(2000))
        if (embed != null) add("embed", embed)
    }).fromJson()
}