package lib.rest.http.httpRequests

import io.ktor.client.response.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Snowflake
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
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
private suspend fun Bot.getRequest(url: String, routeKey: String): HttpResponse =
        request(routeKey, url, HttpMethod.Get, EmptyContent)

/**
 * see [https://discordapp.com/developers/docs/resources/channel#get-channel]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getChannel(id: Snowflake): Channel {
    return channels.computeIfAbsent(id) { getRequest("/channels/$id", "GET-getChannel-$id").fromJson() }
}

/**
 * see [https://discordapp.com/developers/docs/resources/channel#get-channel-messages]
 * If operating on a guild channel, this endpoint requires the `VIEW_CHANNEL` permission to be present on the current user.
 * If the current user is missing the `READ_MESSAGE_HISTORY` permission in the channel then this will return no messages (since they cannot read the message history).
 * @return [Array]<[Message]> on success.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getMessages(getChannelMessages: GetChannelMessages): Array<Message> {
    return getRequest(
            "/channels/${getChannelMessages.channel}/messages?${getChannelMessages.queryParams}",
            "GET-getMessages-${getChannelMessages.channel}"
    )
            .fromJson<Array<Message>>()
            .also { it.asSequence().forEach { messages.add(it) } }
}

/**
 * If operating on a guild channel, this endpoint requires the `READ_MESSAGE_HISTORY` permission to be present on the current user.
 * @return a specific [Message] in the [Channel]
 * see also [https://discordapp.com/developers/docs/resources/channel#get-channel-message]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getMessage(channelId: Snowflake, messageId: Snowflake, forceRequest: Boolean = false): Message {
    val url = "/channels/$channelId/messages/$messageId"
    val routeKey = "GET-getMessage-$channelId"
    return if (forceRequest) {
        getRequest(url, routeKey).fromJson<Message>().let { messages.add(it) }
    } else {
        messages.computeIfAbsent(messageId) { getRequest(url, routeKey).fromJson() }
    }
}

/**
 * @return [Array]<[User]> that reacted with this emoji
 * see also [https://discordapp.com/developers/docs/resources/channel#get-reactions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getReactions(channelId: Snowflake, messageId: Snowflake, emoji: Char): Array<User> {
    return getRequest("/channels/$channelId/messages/$messageId/reactions/$emoji", "GET-getReactions-$channelId").fromJson()
}

/**
 * Pin a message in a channel. Requires the `MANAGE_MESSAGES` permission.
 * The max pinned messages is 50.
 * see also [https://discordapp.com/developers/docs/resources/channel#get-pinned-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getPins(channelId: Snowflake): Array<Message> {
    return getRequest("/channels/$channelId/pins", "GET-getPins-$channelId").fromJson<Array<Message>>().also {
        for (message in it) {
            messages.add(message)
        }
    }
}

/**
 * See also [https://discordapp.com/developers/docs/resources/user#get-user]
 * @return [User] object for a given user ID
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getUser(id: Snowflake): User {
    return users.computeIfAbsent(id) { getRequest("/users/$id", "GET-getUser").fromJson() }
}

/**
 * see also [https://discordapp.com/developers/docs/resources/user#get-current-user-guilds]
 * @param limit max number of guilds to return (1-100)
 * @return list of partial [Guild] objects the current user is a member of; requires the `guilds` OAuth2 scope
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getGuilds(before: Boolean? = null, after: Boolean? = null, limit: Int = 100): Array<Guild> {
    TODO("figure out how the before and after work")
    return getRequest("/users/@me/guilds?", "GET-getGuilds").fromJson()
}

/**
 * see [https://discordapp.com/developers/docs/resources/user#get-user-connections]
 * @return list of [Connection] objects. Requires the `connections` OAuth2 scope
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getUserConnection(): Array<Connection> = getRequest("/users/@me/connections", "GET-getUserConnection").fromJson()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getGuild(id: Snowflake): Guild = guilds.computeIfAbsent(id) { getRequest("/guilds/$id", "GET-getGuild-$id").fromJson() }

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.gateway(): String = getRequest("/gateway/bot", "GET-gateway").fromJson<BotGateway>().url.removePrefix("wss://")

