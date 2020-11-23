package lib.rest.http.httpRequests

import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.exceptions.RequestException
import lib.model.ChannelId
import lib.model.GuildId
import lib.model.IntoId
import lib.model.UserId
import lib.model.channel.Channel
import lib.model.guild.Guild
import lib.model.user.Connection
import lib.model.user.User
import lib.util.fromJson
import lib.util.j
import lib.util.toJson

/**
 * See also [https://discordapp.com/developers/docs/resources/user#get-user]
 * @return [User] object for a given user ID
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun getUser(id: IntoId<UserId>): User {
    val id = id.intoId()
//    val end = when (id) {
//        Bot.user.id -> "@me"
//        else -> id.toString()
//    }
    return Bot.users.computeIfAbsent(id) { getRequest("/users/$id").fromJson() }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun modifyCurrentUser(username: String? = null/*, avatar: ImageData? = null*/): User {
    val json = mapOf(
            "username" to username/*,
            "avatar" to avatar,*/
    ).filterValues { it != null }
    if (json.isNotEmpty()) {
        // TODO validate that a full User obj is returned
        Bot.user = patchRequest("/users/@me", json.toJson()).fromJson()
    }
    return Bot.user
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun getCurrentUserGuilds(/* todo before, after, limit*/): List<Guild> {
    val query = ""
    return getRequest("/users/@me/guilds$query").fromJson()
}

/**
 * Leave a guild
 * @see [https://discordapp.com/developers/docs/resources/user#leave-guild]
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun leaveGuild(id: IntoId<GuildId>) {
    val id = id.intoId()
    val response = deleteRequest("/users/@me/guilds/$id")
    if (response.status != HttpStatusCode.NoContent) {
        throw RequestException("Deleting guild $id did not succeed")
    }
}

/**
 * See [https://discordapp.com/developers/docs/resources/user#create-dm]
 * Create a new DM channel with a user.
 * @param userId the recipient to open a DM channel with
 * @return DM [Channel] object.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun createDM(userId: IntoId<UserId>): Channel {
    // TODO is there a better way to do this? this seems a bit iffy
    val id = ChannelId(userId.intoId().value)
    return Bot.channels.computeIfAbsent(id) {
        postRequest("/users/@me/channels", j { "recipient_id" to "$id" }).fromJson()
    }
}

/**
 * see [https://discordapp.com/developers/docs/resources/user#get-user-connections]
 * @return list of [Connection] objects. Requires the `connections` OAuth2 scope
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun getUserConnection(): Array<Connection> = getRequest("/users/@me/connections").fromJson()