@file:Suppress("ArrayInDataClass")

package lib.rest.model.events.receiveEvents

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import common.util.A
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
import lib.rest.model.events.receiveEvents.Intents.Companion.DIRECT_MESSAGES
import lib.rest.model.events.receiveEvents.Intents.Companion.DIRECT_MESSAGE_REACTIONS
import lib.rest.model.events.receiveEvents.Intents.Companion.DIRECT_MESSAGE_TYPING
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILDS
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_BANS
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_EMOJIS
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_INTEGRATIONS
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_INVITES
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_MEMBERS
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_MESSAGES
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_MESSAGE_REACTIONS
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_MESSAGE_TYPING
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_PRESENCES
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_VOICE_STATES
import lib.rest.model.events.receiveEvents.Intents.Companion.GUILD_WEBHOOKS
import lib.util.fromJson

/**
 * @param P type of the payload attached with this event
 */
sealed class DispatchEvent<P> {
    val actions: ArrayList<suspend P.() -> Unit> = ArrayList()

    //    fun fromJson(payload: JsonElement): P = gson.fromJson(payload, object : TypeToken<P>() {}.type)
    companion object {
        var intents = Intents()
    }
}

/**
 * inline extension, not member function, because `P` needs to be reified for `fromJson`
 */
@Suppress("unused", "NonAsciiCharacters")
inline fun <reified P : Any> DispatchEvent<P>.withJson(payload: JsonElement, λ: P.() -> Unit) = with(payload.fromJson(), λ)

//fun <P : Any> DispatchEvent<P>.fromJson(payload: JsonElement): P = gson.fromJson(payload, object : TypeToken<P>() {}.type)

//suspend inline fun <reified P : Any> DispatchEvent<P>.runAllActions(payload: JsonElement) {
//    val p = payload.fromJson(P::class)
//    for (action in actions) {
//        payload.fromJson(this::class)
//    }
//}

object Ready : DispatchEvent<ReadyPayload>() // Always sent

object Resumed : DispatchEvent<ResumePayload>() // Always sent

//object InvalidSession : DispatchEvent<InvalidSessionPayload>()

object ChannelCreate : DispatchEvent<Channel>() {
    init {
        intents += GUILDS + DIRECT_MESSAGES
    }
}

object ChannelUpdate : DispatchEvent<Channel>() {
    init {
        intents += GUILDS
    }
}

object ChannelDelete : DispatchEvent<Channel>() {
    init {
        intents += GUILDS
    }
}

object ChannelPinsUpdate : DispatchEvent<ChannelPinsPayload>() {
    init {
        intents += GUILDS + DIRECT_MESSAGES
    }
}

object GuildCreate : DispatchEvent<Guild>() {
    init {
        intents += GUILDS
    }
}

object GuildUpdate : DispatchEvent<Guild>() {
    init {
        intents += GUILDS
    }
}

object GuildDelete : DispatchEvent<Guild>() {
    init {
        intents += GUILDS
    }
}

object GuildBanAdd : DispatchEvent<GuildBanUpdatePayload>() {
    init {
        intents += GUILD_BANS
    }
}

object GuildBanRemove : DispatchEvent<GuildBanUpdatePayload>() {
    init {
        intents += GUILD_BANS
    }
}

object GuildEmojisUpdate : DispatchEvent<GuildEmojisPayload>() {
    init {
        intents += GUILD_EMOJIS
    }
}

object GuildIntegrationsUpdate : DispatchEvent<IntegrationsUpdatePayload>() {
    init {
        intents += GUILD_INTEGRATIONS
    }
}

object GuildMemberAdd : DispatchEvent<GuildMember>() {
    init {
        intents += GUILD_MEMBERS
    }
}

object GuildMemberRemove : DispatchEvent<GuildMemberRemovePayload>() {
    init {
        intents += GUILD_MEMBERS
    }
}

object GuildMemberUpdate : DispatchEvent<GuildMemberUpdatePayload>() {
    init {
        intents += GUILD_MEMBERS
    }
}

object GuildMembersChunk : DispatchEvent<GuildMembersChunkPayload>() // always sent

object GuildRoleCreate : DispatchEvent<GuildRoleUpdatePayload>() {
    init {
        intents += GUILDS
    }
}

object GuildRoleUpdate : DispatchEvent<GuildRoleUpdatePayload>() {
    init {
        intents += GUILDS
    }
}

object GuildRoleDelete : DispatchEvent<GuildRoleDeletePayload>() {
    init {
        intents += GUILDS
    }
}

object InviteCreate : DispatchEvent<InviteCreatePayload>() {
    init {
        intents += GUILD_INVITES
    }
}

object InviteDelete : DispatchEvent<InviteDeletePayload>() {
    init {
        intents += GUILD_INVITES
    }
}

object MessageCreate : DispatchEvent<Message>() {
    init {
        intents += GUILD_MESSAGES + DIRECT_MESSAGES
    }
}

object MessageUpdate : DispatchEvent<Message>() {
    init {
        intents += GUILD_MESSAGES + DIRECT_MESSAGES
    }
}

object MessageDelete : DispatchEvent<MessageDeletePayload>() {
    init {
        intents += GUILD_MESSAGES + DIRECT_MESSAGES
    }
}

object MessageDeleteBulk : DispatchEvent<MessageDeleteBulkPayload>() {
    init {
        intents += GUILD_MESSAGES + DIRECT_MESSAGES
    }
}

/**
 * Either [MessageReactionAdd] or [MessageReactionRemove]
 *
 * Not actually sent by Discord
 */
object MessageReactionUpdate : DispatchEvent<MessageReactionUpdatePayload>() {
    init {
        intents += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object MessageReactionAdd : DispatchEvent<MessageReactionAddPayload>() {
    init {
        intents += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object MessageReactionRemove : DispatchEvent<MessageReactionRemovePayload>() {
    init {
        intents += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object MessageReactionRemoveAll : DispatchEvent<MessageReactionRemoveAllPayload>() {
    init {
        intents += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object MessageReactionRemoveEmoji : DispatchEvent<MessageReactionRemoveEmojiPayload>() {
    init {
        intents += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object PresenceUpdate : DispatchEvent<PresenceUpdatePayload>() {
    init {
        intents += GUILD_PRESENCES
    }
}

//object PresencesReplace : DispatchEvent<Array<JsonElement>>()

object TypingStart : DispatchEvent<TypingStartPayload>() {
    init {
        intents += GUILD_MESSAGE_TYPING + DIRECT_MESSAGE_TYPING
    }
}

object UserUpdate : DispatchEvent<User>() // Always sent

object VoiceStateUpdate : DispatchEvent<VoiceState>() {
    init {
        intents += GUILD_VOICE_STATES
    }
}

object VoiceServerUpdate : DispatchEvent<VoiceServerUpdatePayload>()

object WebhookUpdate : DispatchEvent<WebhookUpdatePayload>() {
    init {
        intents += GUILD_WEBHOOKS
    }
}

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

//data class InvalidSessionPayload(
//        val op: GatewayOpcode,
//        @SerializedName("d") val resumable: Boolean
//)

data class MessageDeletePayload(
        val id: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?
) {
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
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

/**
 * Never (de)serialized
 */
data class MessageReactionUpdatePayload(
        val userId: Snowflake,
        val messageId: Snowflake,
        val channelId: Snowflake,
        val guildId: Snowflake?,
        val emoji: Emoji,
        val type: Type
) {
    enum class Type { Add, Remove }
}

data class MessageReactionAddPayload(
        @SerializedName("user_id") val userId: Snowflake,
        @SerializedName("message_id") val messageId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?,
        val member: GuildMember?,
        val emoji: Emoji
)

data class MessageReactionRemovePayload(
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

data class MessageReactionRemoveEmojiPayload(
        @SerializedName("message_id") val messageId: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake? = null,
        val emoji: Emoji
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

data class InviteCreatePayload(
        @SerializedName("channel_id") val channelId: Snowflake,
        val code: String,
        @SerializedName("created_at") val createdAd: Timestamp,
        @SerializedName("guild_id") val guildId: Snowflake? = null,
        val inviter: User? = null,
        @SerializedName("max_age") val maxAge: Int,
        @SerializedName("max_uses") val maxUses: Int,
        @SerializedName("target_user") val targetUser: User? = null,
        @SerializedName("target_user_type") val targetUserType: Int? = null,
        val temporary: Boolean,
        val uses: Int
)

data class InviteDeletePayload(
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake? = null,
        val code: String
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