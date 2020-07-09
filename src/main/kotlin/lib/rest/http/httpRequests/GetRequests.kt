@file:Suppress("NAME_SHADOWING")

package lib.rest.http.httpRequests

import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.*
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.guild.GuildMember
import lib.model.user.Connection
import lib.model.user.User
import lib.rest.http.GetChannelMessages
import lib.rest.model.BotGateway
import lib.util.fromJson

/**
 * behind the scenes requester for get requests
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun getRequest(url: String, routeKey: String): HttpResponse =
        request(routeKey, url, HttpMethod.Get, EmptyContent)

/**
 * see [https://discordapp.com/developers/docs/resources/channel#get-channel]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getChannel(id: IntoId<ChannelId>, forceRequest: Boolean = false): Channel {
    val id = id.intoId()
    val url = "/channels/$id"
    val routeKey = "GET-getChannel-$id"
    return if (forceRequest) {
        getRequest(url, routeKey).fromJson<Channel>().let { Bot.channels.addOrUpdate(it) }
    } else {
        Bot.channels.computeIfAbsent(id) { getRequest(url, routeKey).fromJson() }
    }
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
            "GET-getMessages-${getChannelMessages.channel}"
    )
            .fromJson<Array<Message>>()
            .also { it.asSequence().forEach { Bot.messages.addOrUpdate(it) } }
}

/**
 * If operating on a guild channel, this endpoint requires the `READ_MESSAGE_HISTORY` permission to be present on the current user.
 * @return a specific [Message] in the [Channel]
 * see also [https://discordapp.com/developers/docs/resources/channel#get-channel-message]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getMessage(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, forceRequest: Boolean = false): Message {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    val url = "/channels/$channelId/messages/$messageId"
    val routeKey = "GET-getMessage-$channelId"
    return if (forceRequest) {
        getRequest(url, routeKey).fromJson<Message>().let { Bot.messages.addOrUpdate(it) }
    } else {
        Bot.messages.computeIfAbsent(messageId) { getRequest(url, routeKey).fromJson() }
    }
}

/**
 * @return [Array]<[User]> that reacted with this emoji
 * see also [https://discordapp.com/developers/docs/resources/channel#get-reactions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getReactions(channelId: IntoId<ChannelId>, messageId: IntoId<MessageId>, emoji: Char): Array<User> {
    val channelId = channelId.intoId()
    val messageId = messageId.intoId()
    return getRequest("/channels/$channelId/messages/$messageId/reactions/$emoji", "GET-getReactions-$channelId").fromJson()
}


/**
 * @return [Array]<[Message]> that are pinned
 * see also [https://discord.com/developers/docs/resources/channel#get-pinned-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getPinnedMessages(channelId: IntoId<ChannelId>): Array<Message> {
    val channelId = channelId.intoId()
    return getRequest("/channels/$channelId/pins", "GET-getPinnedReactions-$channelId").fromJson()
}

/**
 * Pin a message in a channel. Requires the `MANAGE_MESSAGES` permission.
 * The max pinned messages is 50.
 * see also [https://discordapp.com/developers/docs/resources/channel#get-pinned-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getPins(channelId: IntoId<ChannelId>): Array<Message> {
    val channelId = channelId.intoId()
    return getRequest("/channels/$channelId/pins", "GET-getPins-$channelId").fromJson<Array<Message>>().also {
        for (message in it) {
            Bot.messages.addOrUpdate(message)
        }
    }
}

/**
 * See also [https://discordapp.com/developers/docs/resources/user#get-user]
 * @return [User] object for a given user ID
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getUser(id: IntoId<UserId>): User {
    val id = id.intoId()
    return Bot.users.computeIfAbsent(id) { getRequest("/users/$id", "GET-getUser").fromJson() }
}

/**
 * see also [https://discordapp.com/developers/docs/resources/user#get-current-user-guilds]
 * @param limit max number of guilds to return (1-100)
 * @return list of partial [Guild] objects the current user is a member of; requires the `guilds` OAuth2 scope
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getGuilds(before: Boolean? = null, after: Boolean? = null, limit: Int = 100): Array<Guild> {
    TODO("figure out how the before and after work")
    return getRequest("/users/@me/guilds?", "GET-getGuilds").fromJson()
}

/**
 * see [https://discordapp.com/developers/docs/resources/user#get-user-connections]
 * @return list of [Connection] objects. Requires the `connections` OAuth2 scope
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getUserConnection(): Array<Connection> = getRequest("/users/@me/connections", "GET-getUserConnection").fromJson()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getGuild(id: IntoId<GuildId>, forceRequest: Boolean = false): Guild {
    val id = id.intoId()
    val url = "/guilds/$id"
    val routeKey = "GET-getGuild-$id"
    return if (forceRequest) {
        getRequest(url, routeKey).fromJson<Guild>().let { Bot.guilds.addOrUpdate(it) }
    } else {
        Bot.guilds.computeIfAbsent(id) { getRequest(url, routeKey).fromJson() }
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getGuildChannels(id: IntoId<GuildId>): Array<Channel> {
    val id = id.intoId()
    val channels = getRequest("/guilds/$id/channels", "GET-getGuildChannels-$id").fromJson<Array<Channel>>()
    // most of the time this will just be fetching from a hashmap
    Bot.guilds.addOrUpdate(getGuild(id).copy(channels = channels))
    return channels
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun getGuildMember(guildId: IntoId<GuildId>, userId: IntoId<UserId>): GuildMember {
    val guildId = guildId.intoId()
    val userId = userId.intoId()
    return getRequest("/guilds/$guildId/members/$userId", "GET-getGuildMember-$guildId").fromJson()
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun gateway(): String = getRequest("/gateway/bot", "GET-gateway").fromJson<BotGateway>().url.removePrefix("wss://")

