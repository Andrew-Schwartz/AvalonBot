package lib.rest.http.httpRequests

import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.exceptions.RequestException
import lib.model.Snowflake
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.api
import lib.rest.client
import lib.rest.model.events.receiveEvents.ChannelDelete
import lib.rest.model.events.receiveEvents.ChannelUpdate
import lib.rest.model.events.receiveEvents.MessageDelete
import lib.util.fromJson

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.deleteRequest(url: String): HttpResponse {
    return client.delete(api + url) {
        authHeaders.forEach { (k, v) ->
            header(k, v)
        }
    }
}

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
suspend fun Bot.closeChannel(channelId: Snowflake): Channel {
    return deleteRequest("/channels/$channelId").fromJson()
}

/**
 * @param userId id of users whose message will be deleted. null to delete this bot's reaction
 * Delete a reaction on a [Message].
 * When deleting the reaction of another user, this endpoint requires the `MANAGE_MESSAGES` permission to be present.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-own-reaction] and [https://discordapp.com/developers/docs/resources/channel#delete-user-reaction]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.deleteReaction(channelId: Snowflake, messageId: Snowflake, emoji: Char, userId: Snowflake? = null) {
    val user = userId?.value ?: "@me"
    deleteRequest("/channels/$channelId/messages/$messageId/reactions/$emoji/$user")
}

/**
 * Requires the `MANAGE_MESSAGES` permission to be present on the current user.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-all-reactions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.deleteAllReactions(channelId: Snowflake, messageId: Snowflake) {
    deleteRequest("/channels/$channelId/messages/$messageId/reactions")
}

/**
 * If operating on a guild channel and trying to delete a message that was not sent by the current user,
 * this endpoint requires the `MANAGE_MESSAGES` permission. Fires a [MessageDelete] Gateway event.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-message]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.deleteMessage(channelId: Snowflake, messageId: Snowflake) {
    deleteRequest("/channels/$channelId/messages/$messageId")
}

/**
 * Delete a channel permission overwrite for a user or role in a channel.
 * Only usable for guild channels. Requires the `MANAGE_ROLES` permission.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-channel-permission]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.deleteChannelPermission(channelId: Snowflake, overwriteId: Snowflake) {
    getChannel(channelId).guildId ?: throw IllegalArgumentException("Only usable for guild channels")

    deleteRequest("/channels/$channelId/permissions/$overwriteId")
}

/**
 * See [https://discordapp.com/developers/docs/resources/user#leave-guild]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.leaveGuild(id: Snowflake) {
    val response = deleteRequest("/users/@me/guilds/$id")
    if (response.status != HttpStatusCode.NoContent) {
        throw RequestException("Deleting guild $id did not succeed")
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.deletePin(channelId: Snowflake, messageId: Snowflake) {
    val response = deleteRequest("/channels/$channelId/pins/$messageId")
    if (response.status != HttpStatusCode.NoContent) {
        throw RequestException("Deleting pin for message $messageId in channel ${getChannel(channelId).name} did not succeed")
    }
}