package lib.rest.model.events.receiveEvents

import com.google.gson.annotations.SerializedName
import lib.model.*
import kotlin.reflect.KClass

enum class DispatchEvent(val eventName: String, val jsonClass: KClass<*>) {
    Ready("READY", ReadyEvent::class),
    ChannelCreate("CHANNEL_CREATE", Channel::class),
    ChannelUpdate("CHANNEL_UPDATE", Channel::class),
    ChannelDelete("CHANNEL_DELETE", Channel::class),
    ChannelPinsUpdate("CHANNEL_PINS_UPDATE", PinsUpdate::class),
    GuildCreate("GUILD_CREATE", Guild::class),
    GuildUpdate("GUILD_UPDATE", Guild::class),
    GuildDelete("GUILD_DELETE", Guild::class),
    GuildBanAdd("GUILD_BAN_ADD", GuildBanUpdate::class),
    GuildBanRemove("GUILD_BAN_REMOVE", GuildBanUpdate::class),
    GuildEmojisUpdate("GUILD_EMOJIS_UPDATE", GuildEmojisEvent::class),
    GuildIntegrationsUpdate("GUILD_INTEGRATIONS_UPDATE", IntegrationsUpdate::class),
    GuildMemberAdd("GUILD_MEMBER_ADD", GuildMember::class),
    GuildMemberRemove("GUILD_MEMBER_REMOVE", GuildMemberRemoveEvent::class),
    GuildMemberUpdate("GUILD_MEMBER_UPDATE", GuildMemberUpdateEvent::class),
    GuildMembersChunk("GUILD_MEMBERS_CHUNK", GuildMembersChunkEvent::class),
    GuildRoleCreate("GUILD_ROLE_CREATE", GuildRoleUpdateEvent::class),
    GuildRoleUpdate("GUILD_ROLE_UPDATE", GuildRoleUpdateEvent::class),
    GuildRoleDelete("GUILD_ROLE_DELETE", GuildRoleDeleteEvent::class),
    MessageCreate("MESSAGE_CREATE", Message::class),
    MessageUpdate("MESSAGE_UPDATE", Message::class),
    MessageDelete("MESSAGE_DELETE", MessageDeleteEvent::class),
    MessageDeleteBulk("MESSAGE_DELETE_BULK", MessageDeleteBulkEvent::class),
    MessageReactionAdd("MESSAGE_REACTION_ADD", MessageReactionUpdate::class),
    MessageReactionRemove("MESSAGE_REACTION_REMOVE", MessageReactionUpdate::class),
    MessageReactionRemoveAll("MESSAGE_REACTION_REMOVE_ALL", MessageReactionRemoveAllEvent::class),
    PresenceUpdate("PRESENCE_UPDATE", PresenceUpdateEvent::class),
    PresencesReplace("PRESENCES_REPLACE", PresenceUpdateEvent::class),
    TypingStart("TYPING_START", TypingStartEvent::class),
    UserUpdate("USER_UPDATE", User::class),
    VoiceStateUpdate("VOICE_STATE_UPDATE", VoiceState::class),
    VoiceServerUpdate("VOICE_SERVER_UPDATE", VoiceServerUpdateEvent::class),
    WebhooksUpdate("WEBHOOKS_UPDATE", WebhookUpdateEvent::class)
}

@Suppress("ArrayInDataClass")
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