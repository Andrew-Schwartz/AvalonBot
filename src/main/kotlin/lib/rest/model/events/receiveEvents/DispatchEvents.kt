@file:Suppress("ArrayInDataClass")

package lib.rest.model.events.receiveEvents

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import common.bot
import common.util.A
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.*
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
import lib.rest.model.events.receiveEvents.Intent.Intents.DIRECT_MESSAGES
import lib.rest.model.events.receiveEvents.Intent.Intents.DIRECT_MESSAGE_REACTIONS
import lib.rest.model.events.receiveEvents.Intent.Intents.DIRECT_MESSAGE_TYPING
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILDS
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_BANS
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_EMOJIS
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_INTEGRATIONS
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_INVITES
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_MEMBERS
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_MESSAGES
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_MESSAGE_REACTIONS
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_MESSAGE_TYPING
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_PRESENCES
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_VOICE_STATES
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_WEBHOOKS
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type
import lib.util.fromJson
import kotlin.reflect.KClass

/**
 * @param P type of the payload attached with this event
 */
sealed class DispatchEvent<P : Any>(private val payloadClass: KClass<out P>) {
    val actions: ArrayList<suspend P.() -> Unit> = ArrayList()

    @Suppress("UNCHECKED_CAST")
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun runActions(payloadData: JsonElement) {
        val payload = when (val payload = payloadData.fromJson(payloadClass)) {
            is Guild -> bot.guilds.addOrUpdate(payload) as P
            is Channel -> bot.channels.addOrUpdate(payload) as P
            is Message -> bot.messages.addOrUpdate(payload) as P
            is User -> bot.users.addOrUpdate(payload) as P
            else -> payload
        }
        actions.forEach { payload.it() }
    }
}

object Ready : DispatchEvent<ReadyPayload>(ReadyPayload::class) // Always sent

object Resumed : DispatchEvent<ResumePayload>(ResumePayload::class) // Always sent

object ChannelCreate : DispatchEvent<Channel>(Channel::class) {
    init {
        Intent += GUILDS + DIRECT_MESSAGES
    }
}

object ChannelUpdate : DispatchEvent<Channel>(Channel::class) {
    init {
        Intent += GUILDS
    }
}

object ChannelDelete : DispatchEvent<Channel>(Channel::class) {
    init {
        Intent += GUILDS
    }
}

object ChannelPinsUpdate : DispatchEvent<ChannelPinsPayload>(ChannelPinsPayload::class) {
    init {
        Intent += GUILDS + DIRECT_MESSAGES
    }
}

object GuildCreate : DispatchEvent<Guild>(Guild::class) {
    init {
        Intent += GUILDS
    }
}

object GuildUpdate : DispatchEvent<Guild>(Guild::class) {
    init {
        Intent += GUILDS
    }
}

object GuildDelete : DispatchEvent<Guild>(Guild::class) {
    init {
        Intent += GUILDS
    }
}

object GuildBanAdd : DispatchEvent<GuildBanUpdatePayload>(GuildBanUpdatePayload::class) {
    init {
        Intent += GUILD_BANS
    }
}

object GuildBanRemove : DispatchEvent<GuildBanUpdatePayload>(GuildBanUpdatePayload::class) {
    init {
        Intent += GUILD_BANS
    }
}

object GuildEmojisUpdate : DispatchEvent<GuildEmojisPayload>(GuildEmojisPayload::class) {
    init {
        Intent += GUILD_EMOJIS
    }
}

object GuildIntegrationsUpdate : DispatchEvent<IntegrationsUpdatePayload>(IntegrationsUpdatePayload::class) {
    init {
        Intent += GUILD_INTEGRATIONS
    }
}

object GuildMemberAdd : DispatchEvent<GuildMember>(GuildMember::class) {
    init {
        Intent += GUILD_MEMBERS
    }
}

object GuildMemberRemove : DispatchEvent<GuildMemberRemovePayload>(GuildMemberRemovePayload::class) {
    init {
        Intent += GUILD_MEMBERS
    }
}

object GuildMemberUpdate : DispatchEvent<GuildMemberUpdatePayload>(GuildMemberUpdatePayload::class) {
    init {
        Intent += GUILD_MEMBERS
    }
}

object GuildMembersChunk : DispatchEvent<GuildMembersChunkPayload>(GuildMembersChunkPayload::class) // always sent

object GuildRoleCreate : DispatchEvent<GuildRoleUpdatePayload>(GuildRoleUpdatePayload::class) {
    init {
        Intent += GUILDS
    }
}

object GuildRoleUpdate : DispatchEvent<GuildRoleUpdatePayload>(GuildRoleUpdatePayload::class) {
    init {
        Intent += GUILDS
    }
}

object GuildRoleDelete : DispatchEvent<GuildRoleDeletePayload>(GuildRoleDeletePayload::class) {
    init {
        Intent += GUILDS
    }
}

object InviteCreate : DispatchEvent<InviteCreatePayload>(InviteCreatePayload::class) {
    init {
        Intent += GUILD_INVITES
    }
}

object InviteDelete : DispatchEvent<InviteDeletePayload>(InviteDeletePayload::class) {
    init {
        Intent += GUILD_INVITES
    }
}

object MessageCreate : DispatchEvent<Message>(Message::class) {
    init {
        Intent += GUILD_MESSAGES + DIRECT_MESSAGES
    }
}

object MessageUpdate : DispatchEvent<Message>(Message::class) {
    init {
        Intent += GUILD_MESSAGES + DIRECT_MESSAGES
    }
}

object MessageDelete : DispatchEvent<MessageDeletePayload>(MessageDeletePayload::class) {
    init {
        Intent += GUILD_MESSAGES + DIRECT_MESSAGES
    }
}

object MessageDeleteBulk : DispatchEvent<MessageDeleteBulkPayload>(MessageDeleteBulkPayload::class) {
    init {
        Intent += GUILD_MESSAGES + DIRECT_MESSAGES
    }
}

/**
 * Either [MessageReactionAdd] or [MessageReactionRemove]
 *
 * Not actually sent by Discord
 */
object MessageReactionUpdate : DispatchEvent<MessageReactionUpdatePayload>(MessageReactionUpdatePayload::class) {
    init {
        Intent += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object MessageReactionAdd : DispatchEvent<MessageReactionAddPayload>(MessageReactionAddPayload::class) {
    init {
        Intent += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object MessageReactionRemove : DispatchEvent<MessageReactionRemovePayload>(MessageReactionRemovePayload::class) {
    init {
        Intent += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object MessageReactionRemoveAll : DispatchEvent<MessageReactionRemoveAllPayload>(MessageReactionRemoveAllPayload::class) {
    init {
        Intent += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object MessageReactionRemoveEmoji : DispatchEvent<MessageReactionRemoveEmojiPayload>(MessageReactionRemoveEmojiPayload::class) {
    init {
        Intent += GUILD_MESSAGE_REACTIONS + DIRECT_MESSAGE_REACTIONS
    }
}

object PresenceUpdate : DispatchEvent<PresenceUpdatePayload>(PresenceUpdatePayload::class) {
    init {
        Intent += GUILD_PRESENCES
    }
}

//object PresencesReplace : DispatchEvent<Array<JsonElement>>()

object TypingStart : DispatchEvent<TypingStartPayload>(TypingStartPayload::class) {
    init {
        Intent += GUILD_MESSAGE_TYPING + DIRECT_MESSAGE_TYPING
    }
}

object UserUpdate : DispatchEvent<User>(User::class) // Always sent

object VoiceStateUpdate : DispatchEvent<VoiceState>(VoiceState::class) {
    init {
        Intent += GUILD_VOICE_STATES
    }
}

object VoiceServerUpdate : DispatchEvent<VoiceServerUpdatePayload>(VoiceServerUpdatePayload::class)

object WebhookUpdate : DispatchEvent<WebhookUpdatePayload>(WebhookUpdatePayload::class) {
    init {
        Intent += GUILD_WEBHOOKS
    }
}

data class ReadyPayload(
        val v: Int,
        val user: User,
        @SerializedName("private_channels") val privateChannels: Array<Channel> = emptyArray(),
        val guilds: Array<Guild> = emptyArray(),
        @SerializedName("session_id") val sessionId: String,
        val _trace: Array<String>,
        val shard: Array<Int> = A[0, 1]
)

data class ResumePayload(
        val _trace: Array<String> // guilds the user is in
) {
    val guildsIds by lazy { _trace.map(::GuildId) }
}

//data class InvalidSessionPayload(
//        val op: GatewayOpcode,
//        @SerializedName("d") val resumable: Boolean
//)

data class MessageDeletePayload(
        val id: MessageId,
        @SerializedName("channel_id") val channelId: ChannelId,
        @SerializedName("guild_id") val guildId: GuildId?
) {
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun message(): Message {
        return bot.getMessage(channelId, id)
    }
}

data class MessageDeleteBulkPayload(
        @SerializedName("ids") private val _ids: Array<String>,
        @SerializedName("channel_id") val channelId: ChannelId,
        @SerializedName("guild_id") val guildId: GuildId?
) {
    val ids by lazy { _ids.map(::MessageId) }
}

data class ChannelPinsPayload(
        @SerializedName("guild_id") val guildId: GuildId?,
        @SerializedName("channel_id") val channelId: ChannelId,
        @SerializedName("last_pin_timestamp") val lastPinTimestamp: Timestamp
)

data class GuildBanUpdatePayload(
        @SerializedName("guild_id") val guildId: GuildId,
        val user: User
)

data class GuildEmojisPayload(
        @SerializedName("guild_id") val guildId: GuildId,
        val emojis: Array<Emoji>
)

/**
 * Never (de)serialized
 */
// TODO should probably be an interface or smth that'd be way cleaner
data class MessageReactionUpdatePayload(
        val userId: UserId,
        val messageId: MessageId,
        val channelId: ChannelId,
        val guildId: GuildId?,
        val emoji: Emoji,
        val type: Type
) {
    enum class Type { Add, Remove }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun user(): User = userId.user()

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun channel(): Channel = channelId.channel()

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun message(): Message = with(channel()) { messageId.message() }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun guild(): Guild? = guildId?.guild()
}

data class MessageReactionAddPayload(
        @SerializedName("user_id") val userId: UserId,
        @SerializedName("message_id") val messageId: MessageId,
        @SerializedName("channel_id") val channelId: ChannelId,
        @SerializedName("guild_id") val guildId: GuildId?,
        val member: GuildMember?,
        val emoji: Emoji
) {
    fun toUpdate(): MessageReactionUpdatePayload =
            MessageReactionUpdatePayload(userId, messageId, channelId, guildId, emoji, Type.Add)
}

data class MessageReactionRemovePayload(
        @SerializedName("user_id") val userId: UserId,
        @SerializedName("message_id") val messageId: MessageId,
        @SerializedName("channel_id") val channelId: ChannelId,
        @SerializedName("guild_id") val guildId: GuildId?,
        val emoji: Emoji
) {
    fun toUpdate(): MessageReactionUpdatePayload =
            MessageReactionUpdatePayload(userId, messageId, channelId, guildId, emoji, Type.Remove)
}

data class MessageReactionRemoveAllPayload(
        @SerializedName("message_id") val messageId: UserId,
        @SerializedName("channel_id") val channelId: MessageId,
        @SerializedName("guild_id") val guildId: GuildId?
)

data class MessageReactionRemoveEmojiPayload(
        @SerializedName("message_id") val messageId: UserId,
        @SerializedName("channel_id") val channelId: MessageId,
        @SerializedName("guild_id") val guildId: GuildId? = null,
        val emoji: Emoji
)

@Suppress("ArrayInDataClass")
data class PresenceUpdatePayload(
        val user: User,
        @SerializedName("roles") private val _roles: Array<String>,
        val game: Activity?,
        @SerializedName("guild_id") val guildId: GuildId,
        val status: String,
        val activities: Array<Activity>,
        @SerializedName("client_status") val clientStatus: ClientStatus
) {
    val roles by lazy { _roles.map(::RoleId) }
}


data class IntegrationsUpdatePayload(
        @SerializedName("guild_id") val guildId: GuildId
)

data class GuildMemberRemovePayload(
        @SerializedName("guild_id") val guildId: GuildId,
        val user: User
)

data class GuildMemberUpdatePayload(
        @SerializedName("guild_id") val guildId: GuildId,
        @SerializedName("roles") private val _roles: Array<String>,
        val user: User,
        val nick: String
) {
    val roles by lazy { _roles.map(::RoleId) }
}

data class GuildMembersChunkPayload(
        @SerializedName("guild_id") val guildId: GuildId,
        val members: Array<GuildMember>
)

data class GuildRoleUpdatePayload(
        @SerializedName("guild_id") val guildId: GuildId,
        val role: Role
)

data class GuildRoleDeletePayload(
        @SerializedName("guild_id") val guildId: GuildId,
        val role: RoleId
)

data class InviteCreatePayload(
        @SerializedName("channel_id") val channelId: ChannelId,
        val code: String,
        @SerializedName("created_at") val createdAd: Timestamp,
        @SerializedName("guild_id") val guildId: GuildId? = null,
        val inviter: User? = null,
        @SerializedName("max_age") val maxAge: Int,
        @SerializedName("max_uses") val maxUses: Int,
        @SerializedName("target_user") val targetUser: User? = null,
        @SerializedName("target_user_type") val targetUserType: Int? = null,
        val temporary: Boolean,
        val uses: Int
)

data class InviteDeletePayload(
        @SerializedName("channel_id") val channelId: ChannelId,
        @SerializedName("guild_id") val guildId: GuildId? = null,
        val code: String
)

data class TypingStartPayload(
        @SerializedName("channel_id") val channelId: ChannelId,
        @SerializedName("guild_id") val guildId: GuildId?,
        @SerializedName("user_id") val userId: UserId,
        val timestamp: Long
)

data class VoiceServerUpdatePayload(
        val token: String,
        @SerializedName("guild_id") val guildId: GuildId,
        val endpoint: String
)

data class WebhookUpdatePayload(
        @SerializedName("guild_id") val guildId: GuildId,
        @SerializedName("channel_id") val channelId: ChannelId
)