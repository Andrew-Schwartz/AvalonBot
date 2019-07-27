@file:Suppress("ArrayInDataClass")

package lib.rest.model.events.receiveEvents

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Activity
import lib.model.Snowflake
import lib.model.Timestamp
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.emoji.Emoji
import lib.model.guild.ClientStatus
import lib.model.guild.Guild
import lib.model.guild.GuildMember
import lib.model.guild.VoiceState
import lib.model.permissions.Role
import lib.model.user.User
import lib.rest.http.httpRequests.getMessage
import lib.rest.model.GatewayOpcode
import lib.util.A
import lib.util.Action
import lib.util.fromJson

/**
 * @param P type of the payload attached with this event
 */
sealed class DispatchEvent<P> {
    val actions: ArrayList<Action<P>> = ArrayList()
}

// inline extension, not member function, because P needs to be reified for `fromJson`
@Suppress("unused")
inline fun <reified P> DispatchEvent<P>.withJson(payload: JsonElement, λ: P.() -> Unit) = with(payload.fromJson(), λ)

suspend inline fun <reified P> DispatchEvent<P>.runAllActions(payload: JsonElement) {
    for (action: Action<P> in actions) {
        payload.fromJson<P>().action()
    }
}

object Ready : DispatchEvent<ReadyPayload>()

object Resumed : DispatchEvent<ResumePayload>()

object InvalidSession : DispatchEvent<InvalidSessionPayload>()

object ChannelCreate : DispatchEvent<Channel>()

object ChannelUpdate : DispatchEvent<Channel>()

object ChannelDelete : DispatchEvent<Channel>()

object ChannelPinsUpdate : DispatchEvent<ChannelPinsPayload>()

object GuildCreate : DispatchEvent<Guild>()

object GuildUpdate : DispatchEvent<Guild>()

object GuildDelete : DispatchEvent<Guild>()

object GuildBanAdd : DispatchEvent<GuildBanUpdatePayload>()

object GuildBanRemove : DispatchEvent<GuildBanUpdatePayload>()

object GuildEmojisUpdate : DispatchEvent<GuildEmojisPayload>()

object GuildIntegrationsUpdate : DispatchEvent<IntegrationsUpdatePayload>()

object GuildMemberAdd : DispatchEvent<GuildMember>()

object GuildMemberRemove : DispatchEvent<GuildMemberRemovePayload>()

object GuildMemberUpdate : DispatchEvent<GuildMemberUpdatePayload>()

object GuildMembersChunk : DispatchEvent<GuildMembersChunkPayload>()

object GuildRoleCreate : DispatchEvent<GuildRoleUpdatePayload>()

object GuildRoleUpdate : DispatchEvent<GuildRoleUpdatePayload>()

object GuildRoleDelete : DispatchEvent<GuildRoleDeletePayload>()

object MessageCreate : DispatchEvent<Message>()

object MessageUpdate : DispatchEvent<Message>()

object MessageDelete : DispatchEvent<MessageDeletePayload>()

object MessageDeleteBulk : DispatchEvent<MessageDeleteBulkPayload>()

object MessageReactionAdd : DispatchEvent<MessageReactionUpdatePayload>()

object MessageReactionRemove : DispatchEvent<MessageReactionUpdatePayload>()

object MessageReactionRemoveAll : DispatchEvent<MessageReactionRemoveAllPayload>()

object PresenceUpdate : DispatchEvent<PresenceUpdatePayload>()

object PresencesReplace : DispatchEvent<Array<JsonElement>>()

object TypingStart : DispatchEvent<TypingStartPayload>()

object UserUpdate : DispatchEvent<User>()

object VoiceStateUpdate : DispatchEvent<VoiceState>()

object VoiceServerUpdate : DispatchEvent<VoiceServerUpdatePayload>()

object WebhookUpdate : DispatchEvent<WebhookUpdatePayload>()

@Suppress("ArrayInDataClass")
data class ReadyPayload(
        val v: Int,
        val user: User,
        @SerializedName("private_channels") val privateChannels: Array<Channel> = emptyArray(),
        val guilds: Array<Guild> = emptyArray(),
        @SerializedName("session_id") val sessionId: String,
        val _trace: Array<String>,
        val shard: Array<Int> = A[0, 1]
)

@Suppress("ArrayInDataClass")
data class ResumePayload(
        val _trace: Array<String> // guilds the user is in
) {
    val guildsIds by lazy { _trace.map(::Snowflake) }
}

//data class UnavailableGuild(
//        val id: Snowflake,
//        val unavailable: Boolean
//) {
//
//}

data class InvalidSessionPayload(
        val op: GatewayOpcode,
        @SerializedName("d") val resumable: Boolean
)

@Suppress("ArrayInDataClass")
data class SomeArray(val array: Array<Any>)

data class MessageDeletePayload(
        val id: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?
) {
    @ExperimentalCoroutinesApi
    @KtorExperimentalAPI
    suspend fun message(bot: Bot): Message {
        return bot.getMessage(channelId, id)
    }
}

@Suppress("ArrayInDataClass")
data class MessageDeleteBulkPayload(
        @SerializedName("ids") private val _ids: Array<String>,
        @SerializedName("channel_id") private val channelId: Snowflake,
        @SerializedName("guild_id") private val guildId: Snowflake?
) {
    val ids by lazy { _ids.map(::Snowflake) }
}

data class ChannelPinsPayload(
        @SerializedName("guild_id") val guildId: Snowflake?,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("last_pin_timestamp") val lastPinTimestamp: Timestamp
)

data class GuildBanUpdatePayload(
        @SerializedName("guild_id") val guildId: Snowflake,
        val user: User
)

@Suppress("ArrayInDataClass")
data class GuildEmojisPayload(
        @SerializedName("guild_id") val guildId: Snowflake,
        val emojis: Array<Emoji>
)

data class MessageReactionUpdatePayload(
        @SerializedName("user_id") val userId: Snowflake,
        @SerializedName("message_id") val messageId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?,
        val emoji: Emoji
)

data class MessageReactionRemoveAllPayload(
        @SerializedName("message_id") val messageId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?
)

@Suppress("ArrayInDataClass")
data class PresenceUpdatePayload(
        val user: User,
        @SerializedName("roles") private val _roles: Array<String>,
        val game: Activity?,
        @SerializedName("guild_id") val guildId: Snowflake,
        val status: String,
        val activities: Array<Activity>,
        @SerializedName("client_status") val clientStatus: ClientStatus
) {
    val roles by lazy { _roles.map(::Snowflake) }
}


data class IntegrationsUpdatePayload(
        @SerializedName("guild_id") val guildId: Snowflake
)

data class GuildMemberRemovePayload(
        @SerializedName("guild_id") val guildId: Snowflake,
        val user: User
)

@Suppress("ArrayInDataClass")
data class GuildMemberUpdatePayload(
        @SerializedName("guild_id") val guildId: Snowflake,
        @SerializedName("roles") private val _roles: Array<String>,
        val user: User,
        val nick: String
) {
    val roles by lazy { _roles.map(::Snowflake) }
}

@Suppress("ArrayInDataClass")
data class GuildMembersChunkPayload(
        @SerializedName("guild_id") val guildId: Snowflake,
        val members: Array<GuildMember>
)

data class GuildRoleUpdatePayload(
        @SerializedName("guild_id") val guildId: Snowflake,
        val role: Role
)

data class GuildRoleDeletePayload(
        @SerializedName("guild_id") val guildId: Snowflake,
        val role: Snowflake
)

data class TypingStartPayload(
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?,
        @SerializedName("user_id") val userId: Snowflake,
        val timestamp: Long
)

data class VoiceServerUpdatePayload(
        val token: String,
        @SerializedName("guild_id") val guildId: Snowflake,
        val endpoint: String
)

data class WebhookUpdatePayload(
        @SerializedName("guild_id") val guildId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake
)