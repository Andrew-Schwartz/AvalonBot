package lib.rest.http.httpRequests

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Snowflake
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.Connection
import lib.model.user.User
import lib.rest.api
import lib.rest.client
import lib.rest.http.GetChannelMessages
import lib.rest.model.BotGateway
import lib.util.fromJson

/**
 * behind the scenes requester for get requests
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
private suspend fun Bot.getRequest(url: String): HttpResponse {
    return client.get(api + url) {
        authHeaders.forEach { (key, value) ->
            header(key, value)
        }
    }
}

/**
 * see [https://discordapp.com/developers/docs/resources/channel#get-channel]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getChannel(id: Snowflake): Channel {
    return channels.computeIfAbsent(id) { getRequest("/channels/$id").fromJson() }
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
    return getRequest("/channels/${getChannelMessages.channel}/messages?${getChannelMessages.queryParams}").fromJson<Array<Message>>().map {
        messages.add(it)
    }.toTypedArray()
//    return getRequest("/channels/${getChannelMessages.channel}/messages?${getChannelMessages.queryParams}").fromJson<Array<Message>>().also {
//        for (message in it) messages.add(message)
//    }
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
    return if (forceRequest) {
        getRequest(url).fromJson<Message>().let { messages.add(it) }
    } else {
        messages.computeIfAbsent(messageId) { getRequest(url).fromJson() }
    }
}

/**
 * @return [Array]<[User]> that reacted with this emoji
 * see also [https://discordapp.com/developers/docs/resources/channel#get-reactions]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getReactions(channelId: Snowflake, messageId: Snowflake, emoji: Char): Array<User> {
    return getRequest("/channels/$channelId/messages/$messageId/reactions/$emoji").fromJson()
}

/**
 * Pin a message in a channel. Requires the `MANAGE_MESSAGES` permission.
 * The max pinned messages is 50.
 * see also [https://discordapp.com/developers/docs/resources/channel#get-pinned-messages]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getPins(channelId: Snowflake): Array<Message> {
    return getRequest("/channels/$channelId/pins").fromJson<Array<Message>>().also {
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
    return users.computeIfAbsent(id) { getRequest("/users/$id").fromJson() }
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
    return getRequest("/users/@me/guilds?").fromJson()
}

/**
 * see [https://discordapp.com/developers/docs/resources/user#get-user-connections]
 * @return list of [Connection] objects. Requires the `connections` OAuth2 scope
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getUserConnectiosn(): Array<Connection> = getRequest("/users/@me/connections").fromJson()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.getGuild(id: Snowflake): Guild = guilds.computeIfAbsent(id) { getRequest("/guilds/$id").fromJson() }

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Bot.gateway(): String = getRequest("/gateway/bot").fromJson<BotGateway>().url.removePrefix("wss://")

