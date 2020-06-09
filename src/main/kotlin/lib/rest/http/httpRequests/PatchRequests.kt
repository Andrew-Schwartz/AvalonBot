@file:Suppress("NAME_SHADOWING")

package lib.rest.http.httpRequests

import com.google.gson.JsonElement
import io.ktor.client.statement.HttpResponse
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.exceptions.RequestException
import lib.model.ChannelId
import lib.model.IntoId
import lib.model.MessageId
import lib.model.channel.Channel
import lib.model.channel.Embed
import lib.model.channel.Message
import lib.rest.http.ModifyChannelOptions
import lib.rest.model.events.receiveEvents.ChannelUpdate
import lib.rest.model.events.receiveEvents.MessageUpdate
import lib.util.fromJson
import lib.util.j
import lib.util.toJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.patchRequest(url: String, routeKey: String, jsonBody: String): HttpResponse =
        request(routeKey, url, HttpMethod.Patch, TextContent(jsonBody, ContentType.Application.Json))

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend inline fun Bot.patchRequest(url: String, routeKey: String, json: JsonElement) = patchRequest(url, routeKey, json.toJson())

/**
 * see [https://discordapp.com/developers/docs/resources/channel#modify-channel]
 * Update a channels settings. Requires the `MANAGE_CHANNELS` permission for the guild.
 * Fires a [ChannelUpdate] event.
 * If modifying a category, individual [ChannelUpdate] events will fire for each child channel that also changes.
 * @return a [Channel] on success
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.modifyChannel(channelId: IntoId<ChannelId>, modifyInfo: ModifyChannelOptions): Result<Channel> {
    val channelId = channelId.intoId()
    val response = patchRequest("/channels/$channelId", "PATCH-modifyChannel-$channelId", (modifyInfo forChannel getChannel(channelId)).toJson())
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
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.editMessage(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, content: String? = null, embed: Embed? = null): Message {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    return patchRequest("/channels/$channelId/messages/$messageId", "PATCH-editMessage-$channelId", j {
        if (content != null)
            "content" to content.take(2000)
        if (embed != null)
            "embed" to embed
    }).fromJson()
}