@file:Suppress("NAME_SHADOWING")

package lib.rest.http.httpRequests

import com.google.gson.JsonElement
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.http.ContentType.*
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.ChannelId
import lib.model.IntoId
import lib.model.MessageId
import lib.model.UserRoleId
import lib.rest.http.EditChannelPermissions
import lib.util.toJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun putRequest(url: String, jsonBody: String = "") {
    request(url, HttpMethod.Put, TextContent(jsonBody, Application.Json))
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend inline fun putRequest(url: String, json: JsonElement) = putRequest(url, json.toJson())

/**
 * Create a reaction for the message.
 * `emoji` takes the form of `name:id` for custom guild emoji, or Unicode characters.
 * This endpoint requires the `READ_MESSAGE_HISTORY` permission to be present on the current user.
 * Additionally, if nobody else has reacted to the message using this emoji, this endpoint requires the `ADD_REACTIONS` permission to be present on the current user.
 * see also [https://discordapp.com/developers/docs/resources/channel#create-reaction]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun createReaction(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, emoji: Char) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    putRequest("/channels/$channelId/messages/$messageId/reactions/$emoji/@me")
}

/**
 * Edit the channel permission overwrites for a user or role in a channel.
 * Only usable for guild channels. Requires the `MANAGE_ROLES` permission.
 * see also [https://discordapp.com/developers/docs/resources/channel#edit-channel-permissions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun editChannelPermissions(channelId: IntoId<ChannelId>, overwriteId: IntoId<UserRoleId>, editChannelPermissions: EditChannelPermissions) {
    val channelId = channelId.intoId()
    val overwriteId = overwriteId.intoId()
    getChannel(channelId).guildId ?: throw IllegalArgumentException("Only usable for guild channels")

    putRequest("/channels/$channelId/permissions/$overwriteId", editChannelPermissions.toJson())
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun addPin(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    putRequest("/channels/$channelId/pins/$messageId")
}