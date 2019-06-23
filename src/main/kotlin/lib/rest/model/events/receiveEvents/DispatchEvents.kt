package lib.rest.model.events.receiveEvents

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import lib.model.*
import lib.rest.model.GatewayOpcode
import lib.util.Action
import lib.util.fromJson

//enum class OldDispatchEvent(val eventName: String) {
//    Ready("READY"),
//    ChannelCreate("CHANNEL_CREATE"),
//    ChannelUpdate("CHANNEL_UPDATE"),
//    ChannelDelete("CHANNEL_DELETE"),
//    ChannelPinsUpdate("CHANNEL_PINS_UPDATE"),
//    GuildCreate("GUILD_CREATE"),
//    GuildUpdate("GUILD_UPDATE"),
//    GuildDelete("GUILD_DELETE"),
//    GuildBanAdd("GUILD_BAN_ADD"),
//    GuildBanRemove("GUILD_BAN_REMOVE"),
//    GuildEmojisUpdate("GUILD_EMOJIS_UPDATE"),
//    GuildIntegrationsUpdate("GUILD_INTEGRATIONS_UPDATE"),
//    GuildMemberAdd("GUILD_MEMBER_ADD"),
//    GuildMemberRemove("GUILD_MEMBER_REMOVE"),
//    GuildMemberUpdate("GUILD_MEMBER_UPDATE"),
//    GuildMembersChunk("GUILD_MEMBERS_CHUNK"),
//    GuildRoleCreate("GUILD_ROLE_CREATE"),
//    GuildRoleUpdate("GUILD_ROLE_UPDATE"),
//    GuildRoleDelete("GUILD_ROLE_DELETE"),
//    MessageCreate("MESSAGE_CREATE"),
//    MessageUpdate("MESSAGE_UPDATE"),
//    MessageDelete("MESSAGE_DELETE"),
//    MessageDeleteBulk("MESSAGE_DELETE_BULK"),
//    MessageReactionAdd("MESSAGE_REACTION_ADD"),
//    MessageReactionRemove("MESSAGE_REACTION_REMOVE"),
//    MessageReactionRemoveAll("MESSAGE_REACTION_REMOVE_ALL"),
//    PresenceUpdate("PRESENCE_UPDATE"),
//    PresencesReplace("PRESENCES_REPLACE"),
//    TypingStart("TYPING_START"),
//    UserUpdate("USER_UPDATE"),
//    VoiceStateUpdate("VOICE_STATE_UPDATE"),
//    VoiceServerUpdate("VOICE_SERVER_UPDATE"),
//    WebhooksUpdate("WEBHOOKS_UPDATE")
//}

/**
 * @param P type of the payload attached with this event
 */
sealed class DispatchEvent<P> {
    val actions: ArrayList<Action<P>> = arrayListOf()
}

// inline extension, not member function, because P needs to be reified for `fromJson`
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

object MessageCreate : DispatchEvent<Message>()

object MessageUpdate : DispatchEvent<Message>()

@Suppress("ArrayInDataClass")
data class ReadyPayload(
        val v: Int,
        val user: User,
        @SerializedName("private_channels") val privateChannels: Array<Channel> = arrayOf(),
        val guilds: Array<Guild> = arrayOf(),
        @SerializedName("session_id") val sessionId: String,
        val _trace: Array<String>,
        val shard: Array<Int> = arrayOf(0, 1)
)

@Suppress("ArrayInDataClass")
data class ResumePayload(
        val _trace: Array<String> // guilds the user is in, should maybe be snowflakes
)

data class InvalidSessionPayload(
        val op: GatewayOpcode,
        @SerializedName("d") val resumable: Boolean
)

//data class MessageDeleteEvent(
//        val id: Snowflake,
//        @SerializedName("channel_id") val channelId: Snowflake,
//        @SerializedName("guild_id") val guildId: Snowflake?
//) : DispatchEvent()
//
//@Suppress("ArrayInDataClass")
//data class MessageDeleteBulkEvent(
//        @SerializedName("ids") private val _ids: Array<String>,
//        @SerializedName("channel_id") private val channelId: Snowflake,
//        @SerializedName("guild_id") private val guildId: Snowflake?
//) : DispatchEvent() {
//    val ids: Array<Snowflake> by lazy { _ids.map(::Snowflake).toTypedArray() }
//}

data class ChannelPinsPayload(
        @SerializedName("guild_id") val guildId: Snowflake?,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("last_pin_timestamp") val lastPinTimestamp: Timestamp
)

//data class GuildBanUpdateEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        val user: User
//) : DispatchEvent()
//
//@Suppress("ArrayInDataClass")
//data class GuildEmojisEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        val emojis: Array<Emoji>
//) : DispatchEvent()
//
//data class MessageReactionUpdateEvent(
//        @SerializedName("user_id") val userId: Snowflake,
//        @SerializedName("message_id") val messageId: Snowflake,
//        @SerializedName("channel_id") val channelId: Snowflake,
//        @SerializedName("guild_id") val guildId: Snowflake?,
//        val emoji: Emoji
//) : DispatchEvent()
//
//data class MessageReactionRemoveAllEvent(
//        @SerializedName("message_id") val messageId: Snowflake,
//        @SerializedName("channel_id") val channelId: Snowflake,
//        @SerializedName("guild_id") val guildId: Snowflake?
//) : DispatchEvent()
//
//data class IntegrationsUpdateEvent(
//        @SerializedName("guild_id") val guildId: Snowflake
//) : DispatchEvent()
//
//data class GuildMemberRemoveEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        val user: User
//) : DispatchEvent()
//
//@Suppress("ArrayInDataClass")
//data class GuildMemberUpdateEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        @SerializedName("roles") private val _roles: Array<String>,
//        val user: User,
//        val nick: String
//) : DispatchEvent() {
//    val roles: Array<Snowflake> by lazy { _roles.map(::Snowflake).toTypedArray() }
//}
//
//@Suppress("ArrayInDataClass")
//data class GuildMembersChunkEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        val members: Array<GuildMember>
//) : DispatchEvent()
//
//data class GuildRoleUpdateEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        val role: Role
//) : DispatchEvent()
//
//data class GuildRoleDeleteEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        val role: Snowflake
//) : DispatchEvent()
//
//data class TypingStartEvent(
//        @SerializedName("channel_id") val channelId: Snowflake,
//        @SerializedName("guild_id") val guildId: Snowflake?,
//        @SerializedName("user_id") val userId: Snowflake,
//        val timestamp: Long
//) : DispatchEvent()
//
//data class VoiceServerUpdateEvent(
//        val token: String,
//        @SerializedName("guild_id") val guildId: Snowflake,
//        val endpoint: String
//) : DispatchEvent()
//
//data class WebhookUpdateEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        @SerializedName("channel_id") val channelId: Snowflake
//) : DispatchEvent()