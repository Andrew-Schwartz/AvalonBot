package lib.rest.http.httpRequests

import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.exceptions.RequestException
import lib.model.*
import lib.model.channel.Channel
import lib.model.channel.Embed
import lib.model.channel.Message
import lib.model.user.User
import lib.rest.http.CreateMessage
import lib.rest.http.EditChannelPermissions
import lib.rest.http.GetChannelMessages
import lib.rest.http.ModifyChannelOptions
import lib.rest.model.events.receiveEvents.ChannelDelete
import lib.rest.model.events.receiveEvents.ChannelUpdate
import lib.util.fromJson
import lib.util.j
import lib.util.toJson

/**
 * Get a [Channel] object by id
 * @param forceRequest ignore any cached channel with specified [id] and make always make a web request
 * @see [https://discordapp.com/developers/docs/resources/channel#get-channel]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getChannel(id: IntoId<ChannelId>, forceRequest: Boolean = false): Channel {
    val id = id.intoId()
    val url = "/channels/$id"
    return if (forceRequest) {
        getRequest(url).fromJson<Channel>().let { Bot.channels.addOrUpdate(it) }
    } else {
        Bot.channels.computeIfAbsent(id) { getRequest(url).fromJson() }
    }
}

/**
 * see [https://discordapp.com/developers/docs/resources/channel#modify-channel]
 * Update a channels settings. Requires the `MANAGE_CHANNELS` permission for the guild.
 * Fires a [ChannelUpdate] event.
 * If modifying a category, individual [ChannelUpdate] events will fire for each child channel that also changes.
 * @return a [Channel] on success
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun modifyChannel(channelId: IntoId<ChannelId>, modifyInfo: ModifyChannelOptions): Result<Channel> {
    val channelId = channelId.intoId()
    val response = patchRequest("/channels/$channelId", (modifyInfo forChannel getChannel(channelId)).toJson())
    return when (response.status) {
        HttpStatusCode.BadRequest -> Result.failure(RequestException("400 Bad Request, Invalid parameters"))
        else -> Result.success(response.fromJson())
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
suspend fun closeChannel(channelId: IntoId<ChannelId>): Channel {
    val channelId = channelId.intoId()
    return deleteRequest("/channels/$channelId").fromJson()
}

/**
 * If operating on a guild channel, this endpoint requires the `VIEW_CHANNEL` permission to be present on the current user.
 * If the current user is missing the `READ_MESSAGE_HISTORY` permission in the channel then this will return no messages (since they cannot read the message history).
 * @return [Array]<[Message]> on success.
 * @see [https://discordapp.com/developers/docs/resources/channel#get-channel-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getMessages(getChannelMessages: GetChannelMessages): Array<Message> {
    return getRequest(
            "/channels/${getChannelMessages.channel}/messages?${getChannelMessages.queryParams}",
    ).fromJson<Array<Message>>()
            .also { it.asSequence().forEach { Bot.messages.addOrUpdate(it) } }
}

/**
 * If operating on a guild channel, this endpoint requires the `READ_MESSAGE_HISTORY` permission to be present on the current user.
 * @return a specific [Message] in the [Channel]
 * @see [https://discordapp.com/developers/docs/resources/channel#get-channel-message]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getMessage(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, forceRequest: Boolean = false): Message {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    val url = "/channels/$channelId/messages/$messageId"
    return if (forceRequest) {
        getRequest(url).fromJson<Message>().let { Bot.messages.addOrUpdate(it) }
    } else {
        Bot.messages.computeIfAbsent(messageId) { getRequest(url).fromJson() }
    }
}

/**
 * Post a message to a guild text or DM channel.
 * If operating on a guild channel, this endpoint requires the `SEND_MESSAGES` permission to be present on the current user.
 * If [CreateMessage.tts] is set to true, the `SEND_TTS_MESSAGES` permission is required for the message to be spoken.
 * Fires a Message Create Gateway event. See message formatting for more information on how to properly format messages.
 * @return the [Message] that was sent
 * @see [https://discordapp.com/developers/docs/resources/channel#create-message]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun createMessage(channelId: IntoId<ChannelId>, createMessage: CreateMessage): Message {
    val (content, _, _, files, embed) = createMessage
    val channelId = channelId.intoId();
    val url = "/channels/${channelId}/messages"

    val response = if (files == null) {
        postRequest(url, createMessage.toJson(), channelId)
    } else {
        postFormDataRequest(url, channelId) {
            if (content.isNotEmpty() || embed != null) {
                val payload = createMessage.copy(files = null).toJson()
                append("payload_json", payload)
            }

            files.forEach { (name, file) ->
                val bytes: ByteArray = file.readAllBytes()
                append("file", name, ContentType.MultiPart.FormData, bytes.size.toLong()) {
                    bytes.forEach(::writeByte)
                }
            }
        }
    }

    return response.fromJson<Message>().let(Bot.messages::addOrUpdate)
}

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
 * Delete a reaction on a [Message].
 * When deleting the reaction of another user, this endpoint requires the `MANAGE_MESSAGES` permission to be present.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-own-reaction] and [https://discordapp.com/developers/docs/resources/channel#delete-user-reaction]
 * @param userId id of users whose message will be deleted. null to delete this bot's reaction
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteReaction(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, emoji: Char, userId: IntoId<UserId>? = null) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    val user = userId?.intoId()?.value ?: "@me"
    deleteRequest("/channels/$channelId/messages/$messageId/reactions/$emoji/$user")
}

/**
 * @return [Array]<[User]> that reacted with this emoji
 *
 * @see [https://discordapp.com/developers/docs/resources/channel#get-reactions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getReactions(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, emoji: Char): Array<User> {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    return getRequest("/channels/$channelId/messages/$messageId/reactions/$emoji").fromJson()
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
    deleteRequest("/channels/$channelId/messages/$messageId/reactions")
}

/**
 * Requires the `MANAGE_MESSAGES` permission to be present on the current user.
 * see also [https://discordapp.com/developers/docs/resources/channel#delete-all-reactions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteAllReactionsByEmoji(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, emoji: Char) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    deleteRequest("/channels/$channelId/messages/$messageId/reactions/$emoji")
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
suspend fun editMessage(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, content: String? = null, embed: Embed? = null): Message {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    return patchRequest("/channels/$channelId/messages/$messageId", j {
        if (content != null)
            "content" to content.take(2000)
        if (embed != null)
            "embed" to embed
    }).fromJson()
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
    deleteRequest("/channels/$channelId/messages/$messageId")
}

/**
 * Delete multiple messages in a single request.
 * This endpoint can only be used on guild channels and requires the `MANAGE_MESSAGES` permission.
 * Fires multiple Message Delete Gateway events.
 * Any message IDs given that do not exist or are invalid will count towards the minimum and maximum message count (currently 2 and 100 respectively).
 * Additionally, duplicated IDs will only be counted once.
 * see also [https://discordapp.com/developers/docs/resources/channel#bulk-delete-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun bulkDeleteMessages(channelId: IntoId<ChannelId>, messages: Set<Message>) {
    if (messages.size < 2) throw IllegalArgumentException("At least 2 messages are needed for bulk deletion")

    val channelId = channelId.intoId()
    val array: Array<Message> = messages.take(100).toTypedArray()
    postRequest("/channels/$channelId/messages/bulk-delete", j { "messages" to array })
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

    deleteRequest("/channels/$channelId/permissions/$overwriteId")
}

/**
 * Post a typing indicator for the specified channel.
 * Generally bots should **not** implement this route.
 * However, if a bot is responding to a command and expects the computation to take a few seconds,
 * this endpoint may be called to let the user know that the bot is processing their message.
 * Fires a [TypingStart] Gateway event.
 * see also [https://discordapp.com/developers/docs/resources/channel#trigger-typing-indicator]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun triggerTypingIndicator(channelId: IntoId<ChannelId>) {
    val channelId = channelId.intoId()
    postRequest("/channels/$channelId/typing")
}

/**
 * @return [Array]<[Message]> that are pinned
 *
 * @see [https://discord.com/developers/docs/resources/channel#get-pinned-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getPinnedMessages(channelId: IntoId<ChannelId>): Array<Message> {
    val channelId = channelId.intoId()
    return getRequest("/channels/$channelId/pins").fromJson<Array<Message>>().also {
        for (message in it) {
            Bot.messages.addOrUpdate(message)
        }
    }
}

/**
 * Pin a message in a channel. Requires the `MANAGE_MESSAGES` permission.
 * The max pinned messages is 50.
 * @see [https://discordapp.com/developers/docs/resources/channel#get-pinned-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun addPin(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    putRequest("/channels/$channelId/pins/$messageId")
}

/**
 * Delete a pinned message in a channel. Requires the MANAGE_MESSAGES permission.
 * Returns a 204 empty response on success.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deletePin(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>) {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    val response = deleteRequest("/channels/$channelId/pins/$messageId")
    if (response.status != HttpStatusCode.NoContent) {
        throw RequestException("Deleting pin for message $messageId in channel ${getChannel(channelId).name} did not succeed")
    }
}