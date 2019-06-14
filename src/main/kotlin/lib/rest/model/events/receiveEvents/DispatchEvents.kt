package lib.rest.model.events.receiveEvents

import com.google.gson.annotations.SerializedName
import lib.dsl.DiscordDsl
import lib.model.*

enum class DispatchEvent(val eventName: String) {
    Ready("READY"),
    ChannelCreate("CHANNEL_CREATE"),
    ChannelUpdate("CHANNEL_UPDATE"),
    ChannelDelete("CHANNEL_DELETE"),
    ChannelPinsUpdate("CHANNEL_PINS_UPDATE"),
    GuildCreate("GUILD_CREATE"),
    GuildUpdate("GUILD_UPDATE"),
    GuildDelete("GUILD_DELETE"),
    GuildBanAdd("GUILD_BAN_ADD"),
    GuildBanRemove("GUILD_BAN_REMOVE"),
    GuildEmojisUpdate("GUILD_EMOJIS_UPDATE"),
    GuildIntegrationsUpdate("GUILD_INTEGRATIONS_UPDATE"),
    GuildMemberAdd("GUILD_MEMBER_ADD"),
    GuildMemberRemove("GUILD_MEMBER_REMOVE"),
    GuildMemberUpdate("GUILD_MEMBER_UPDATE"),
    GuildMembersChunk("GUILD_MEMBERS_CHUNK"),
    GuildRoleCreate("GUILD_ROLE_CREATE"),
    GuildRoleUpdate("GUILD_ROLE_UPDATE"),
    GuildRoleDelete("GUILD_ROLE_DELETE"),
    MessageCreate("MESSAGE_CREATE"),
    MessageUpdate("MESSAGE_UPDATE"),
    MessageDelete("MESSAGE_DELETE"),
    MessageDeleteBulk("MESSAGE_DELETE_BULK"),
    MessageReactionAdd("MESSAGE_REACTION_ADD"),
    MessageReactionRemove("MESSAGE_REACTION_REMOVE"),
    MessageReactionRemoveAll("MESSAGE_REACTION_REMOVE_ALL"),
    PresenceUpdate("PRESENCE_UPDATE"),
    PresencesReplace("PRESENCES_REPLACE"),
    TypingStart("TYPING_START"),
    UserUpdate("USER_UPDATE"),
    VoiceStateUpdate("VOICE_STATE_UPDATE"),
    VoiceServerUpdate("VOICE_SERVER_UPDATE"),
    WebhooksUpdate("WEBHOOKS_UPDATE")
}

@Suppress("ArrayInDataClass")
@DiscordDsl
data class ReadyEvent(
        val v: Int,
        val user: User,
        @SerializedName("private_channels") val privateChannels: Array<Channel> = arrayOf(),
        val guilds: Array<Guild> = arrayOf(),
        @SerializedName("session_id") val sessionId: String,
        val _trace: Array<String>,
        val shard: Array<Int> = arrayOf(0, 1)
)

data class MessageDeleteEvent(
        val id: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?
)

@Suppress("ArrayInDataClass")
data class MessageDeleteBulkEvent(
        @SerializedName("ids") private val _ids: Array<String>,
        @SerializedName("channel_id") private val channelId: Snowflake,
        @SerializedName("guild_id") private val guildId: Snowflake?
) {

    val ids: Array<Snowflake> by lazy { _ids.map { Snowflake(it) }.toTypedArray() }
}

data class PinsUpdate(
        @SerializedName("guild_id") val guildId: Snowflake?,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("last_pin_timestamp") val lastPinTimestamp: Timestamp
)

data class GuildBanUpdate(
        @SerializedName("guild_id") val guildId: Snowflake,
        val user: User
)

@Suppress("ArrayInDataClass")
data class GuildEmojisEvent(
        @SerializedName("guild_id") val guildId: Snowflake,
        val emojis: Array<Emoji>
)

data class MessageReactionUpdate(
        @SerializedName("user_id") val userId: Snowflake,
        @SerializedName("message_id") val messageId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?,
        val emoji: Emoji
)

data class MessageReactionRemoveAllEvent(
        @SerializedName("message_id") val messageId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?
)

data class IntegrationsUpdate(
        @SerializedName("guild_id") val guildId: Snowflake
)

data class GuildMemberRemoveEvent(
        @SerializedName("guild_id") val guildId: Snowflake,
        val user: User
)

@Suppress("ArrayInDataClass")
data class GuildMemberUpdateEvent(
        @SerializedName("guild_id") val guildId: Snowflake,
        @SerializedName("roles") private val _roles: Array<String>,
        val user: User,
        val nick: String
) {

    val roles: Array<Snowflake> by lazy { _roles.map(::Snowflake).toTypedArray() }
}

@Suppress("ArrayInDataClass")
data class GuildMembersChunkEvent(
        @SerializedName("guild_id") val guildId: Snowflake,
        val members: Array<GuildMember>
)

data class GuildRoleUpdateEvent(
        @SerializedName("guild_id") val guildId: Snowflake,
        val role: Role
)

data class GuildRoleDeleteEvent(
        @SerializedName("guild_id") val guildId: Snowflake,
        val role: Snowflake
)

data class TypingStartEvent(
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?,
        @SerializedName("user_id") val userId: Snowflake,
        val timestamp: Long
)

data class VoiceServerUpdateEvent(
        val token: String,
        @SerializedName("guild_id") val guildId: Snowflake,
        val endpoint: String
)

data class WebhookUpdateEvent(
        @SerializedName("guild_id") val guildId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake
)