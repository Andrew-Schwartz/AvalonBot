package lib.rest.model.events.receiveEvents

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import lib.model.*
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

object TypingStart : DispatchEvent<TypingStartPayload>()

object PresencesReplace : DispatchEvent<EmptyPayload>()

object MessageReactionAdd : DispatchEvent<MessageReactionUpdatePayload>()

object MessageReactionRemove : DispatchEvent<MessageReactionUpdatePayload>()

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
        val _trace: Array<String> // guilds the user is in, should maybe be snowflakes
)

data class InvalidSessionPayload(
        val op: GatewayOpcode,
        @SerializedName("d") val resumable: Boolean
)

class EmptyPayload

//data class MessageDeleteEvent(
//        val id: Snowflake,
//        @SerializedName("channel_id") val channelId: Snowflake,
//        @SerializedName("guild_id") val guildId: Snowflake?
//) : DispatchEvent()
//
//@Suppress("ArrayInDataClass")
//data class MessageDeleteBulkEvent(
//        @SerializedName("ids") private val _ids: A<String>,
//        @SerializedName("channel_id") private val channelId: Snowflake,
//        @SerializedName("guild_id") private val guildId: Snowflake?
//) : DispatchEvent() {
//    val ids: A<Snowflake> by lazy { _ids.map(::Snowflake).toTypedArray() }
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
//        val emojis: A<Emoji>
//) : DispatchEvent()

data class MessageReactionUpdatePayload(
        @SerializedName("user_id") val userId: Snowflake,
        @SerializedName("message_id") val messageId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?,
        val emoji: Emoji
)

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
//        @SerializedName("roles") private val _roles: A<String>,
//        val user: User,
//        val nick: String
//) : DispatchEvent() {
//    val roles: A<Snowflake> by lazy { _roles.map(::Snowflake).toTypedArray() }
//}
//
//@Suppress("ArrayInDataClass")
//data class GuildMembersChunkEvent(
//        @SerializedName("guild_id") val guildId: Snowflake,
//        val members: A<GuildMember>
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

data class TypingStartPayload(
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?,
        @SerializedName("user_id") val userId: Snowflake,
        val timestamp: Long
)

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