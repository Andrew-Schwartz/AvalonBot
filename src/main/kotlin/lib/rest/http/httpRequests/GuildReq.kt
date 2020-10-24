package lib.rest.http.httpRequests

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.GuildId
import lib.model.IntoId
import lib.model.UserId
import lib.model.channel.Channel
import lib.model.guild.Guild
import lib.model.guild.GuildMember
import lib.model.permissions.Role
import lib.util.fromJson
import lib.util.j

/**
 * Returns the guild object for the given id. If [withCounts] is set to true, this endpoint will also return
 * [Guild].approximateMemberCount and [Guild].approximatePresenceCount for the guild.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun getGuild(id: IntoId<GuildId>, withCounts: Boolean = false, forceRequest: Boolean = false): Guild {
    val id = id.intoId()
    val url = buildString {
        append("/guilds/$id")
        if (withCounts) append("?with_counts=true")
    }
    return if (forceRequest) {
        getRequest(url).fromJson<Guild>().let { Bot.guilds.addOrUpdate(it) }
    } else {
        Bot.guilds.computeIfAbsent(id) { getRequest(url).fromJson() }
    }
}

/**
 * Returns a list of guild [Channel] objects.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun getGuildChannels(id: IntoId<GuildId>): Array<Channel> {
    val id = id.intoId()
    val channels = getRequest("/guilds/$id/channels").fromJson<Array<Channel>>()
    // most of the time this will just be fetching from a hashmap
    Bot.guilds.addOrUpdate(getGuild(id).copy(channels = channels))
    return channels
}

/**
 * Returns a [GuildMember] object for the specified user in the specified [Guild].
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun getGuildMember(guildId: IntoId<GuildId>, userId: IntoId<UserId>): GuildMember {
    val guildId = guildId.intoId()
    val userId = userId.intoId()
    return getRequest("/guilds/$guildId/members/$userId").fromJson()
}

/**
 * Returns a list of [GuildMember] objects that are members of the [Guild].
 *
 * In the future, this endpoint will be restricted in line with Discord's Privileged Intents
 *
 * @param limit max number of members to return (1-1000)
 * @param after the highest user id in the previous page
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun getGuildMembers(id: IntoId<GuildId>, limit: Int = 100, after: IntoId<UserId>? = null): Array<GuildMember> {
    val url = buildString {
        append("/guilds/${id.intoId()}/members")
        if (limit != 1) { // Discord's default
            append("?limit=$limit")
        }
        if (after != null) {
            append(if (limit == 1) "?" else "&")
            append("after=${after.intoId()}")
        }
    }
    return getRequest(url).fromJson()
}

/**
 * Modifies the nickname of the current user in a guild. Returns a 200 with the nickname on success.
 * Fires a Guild Member Update Gateway event.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun modifyNick(id: IntoId<GuildId>, nick: String) {
    patchRequest("/guilds/${id.intoId()}/members/@me/nick", j { "nick" to nick })
}

/**
 * Returns a list of [Role] objects for the guild.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun getRoles(id: IntoId<GuildId>): List<Role> = getRequest("/guilds/${id.intoId()}/roles").fromJson()

