@file:Suppress("NAME_SHADOWING")

package lib.rest.http.httpRequests

import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.exceptions.RequestException
import lib.model.*
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.model.events.receiveEvents.ChannelDelete
import lib.rest.model.events.receiveEvents.ChannelUpdate
import lib.rest.model.events.receiveEvents.MessageDelete
import lib.util.fromJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun deleteRequest(url: String, routeKey: String): HttpResponse =
        request(routeKey, url, HttpMethod.Delete, EmptyContent)

/**
 * see [https://discordapp.com/developers/docs/resources/channel#deleteclose-channel]
 * Delete a [Channel], or close a private message.
 * Requires the `MANAGE_CHANNELS` permission for the guild.
 * Deleting a category does not delete its child channels; they will have their `parent_id` removed and a [ChannelUpdate] Gateway event will fire for each of them.
 * Deleting a guild channel cannot be undone. Use this with caution, as it is impossible to undo this action when performed on a guild channel.
 * In contrast, when used with a private message, it is possible to undo the action by opening a private message with the recipient again.
 * @return a [Channel] object on success. Fires a [ChannelDelete] Gateway event.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun closeChannel(channelId: IntoId<ChannelId>): Channel {
    val channelId = channelId.intoId()
    return deleteRequest("/channels/$channelId", "DELETE-closeChannel-$channelId").fromJson()
}

/**
 * @param userId id of users whose message will be deleted. null to delete this bot's reaction
 * Delete a reaction on a [Message].
 * When deleting the reaction of another user, this endpoint requires the `MANAGE_MESSAGES` permission to be present.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-own-reaction] and [https://discordapp.com/developers/docs/resources/channel#delete-user-reaction]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteReaction(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, emoji: Char, userId: IntoId<UserId>? = null) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    val user = userId?.intoId()?.value ?: "@me"
    deleteRequest("/channels/$channelId/messages/$messageId/reactions/$emoji/$user", "DELETE-deleteReaction-$channelId")
}

/**
 * Requires the `MANAGE_MESSAGES` permission to be present on the current user.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-all-reactions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteAllReactions(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    deleteRequest("/channels/$channelId/messages/$messageId/reactions", "DELETE-deleteAllReactions-$channelId")
}

/**
 * If operating on a guild channel and trying to delete a message that was not sent by the current user,
 * this endpoint requires the `MANAGE_MESSAGES` permission. Fires a [MessageDelete] Gateway event.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-message]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteMessage(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    deleteRequest("/channels/$channelId/messages/$messageId", "DELETE-deleteMessage-$channelId")
}

/**
 * Delete a channel permission overwrite for a user or role in a channel.
 * Only usable for guild channels. Requires the `MANAGE_ROLES` permission.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-channel-permission]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteChannelPermission(channelId: IntoId<ChannelId>, overwriteId: IntoId<UserRoleId>) {
    val channelId = channelId.intoId()
    val overwriteId = overwriteId.intoId()
    getChannel(channelId).guildId ?: throw IllegalArgumentException("Only usable for guild channels")

    deleteRequest("/channels/$channelId/permissions/$overwriteId", "DELETE-deleteChannelPermission-$channelId")
}

/**
 * See [https://discordapp.com/developers/docs/resources/user#leave-guild]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun leaveGuild(id: IntoId<GuildId>) {
    val id = id.intoId()
    val response = deleteRequest("/users/@me/guilds/$id", "DELETE-leaveGuild-$id")
    if (response.status != HttpStatusCode.NoContent) {
        throw RequestException("Deleting guild $id did not succeed")
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deletePin(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    val response = deleteRequest("/channels/$channelId/pins/$messageId", "DELETE-deletePin-$channelId")
    if (response.status != HttpStatusCode.NoContent) {
        throw RequestException("Deleting pin for message $messageId in channel ${getChannel(channelId).name} did not succeed")
    }
}