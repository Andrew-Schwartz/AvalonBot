package lib.model.channel

import com.google.gson.annotations.SerializedName
import common.bot
import common.util.listGrammatically
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.*
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.httpRequests.getMessage

@Suppress("ArrayInDataClass")
data class Channel(
        override val id: ChannelId,
        val type: ChannelType,
        @SerializedName("guild_id") val guildId: GuildId?,
        val position: Int?,
        @SerializedName("permission_overwrites") val permissionOverwrites: Array<Overwrite>,
        val name: String?,
        val topic: String?,
        val nsfw: Boolean?,
        @SerializedName("last_message_id") val lastMessageId: MessageId?,
        val bitrate: Int?,
        @SerializedName("userLimit") val userLimit: Int?,
        @SerializedName("rate_limit_per_user") val rateLimitPerUser: Int?,
        val recipients: Array<User>?,
        val icon: String?,
        @SerializedName("owner_id") val ownerId: UserId?,
        @SerializedName("application_id") val applicationId: ApplicationId?,
        @SerializedName("parent_id") val parentId: ChannelId?,
        @SerializedName("last_pin_timestamp") val lastPinTimestamp: Timestamp?
) : Storable<Channel>, IntoId<ChannelId> {
    override fun equals(other: Any?): Boolean = (other as? Channel)?.id == id

    override fun intoId(): ChannelId = id

    override fun hashCode(): Int = id.hashCode()

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun lastMessage(): Message? {
        return bot.getMessage(id, lastMessageId ?: return null)
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun guild(): Guild? = guildId?.guild()

    /**
     * In a guild, gets the channel name:
     *
     * ex: "AwesomeGuild/BotChannel"
     *
     * In a DM, gets the target user(s)
     *
     * ex: "DM: Bob the Builder(, Sally, and George Washington)"
     */
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun fullName(): String {
        return guild()?.let {
            "${it.name}/$name"
        } ?: "DM: ${recipients?.listGrammatically { it.username }}"
    }

    @Suppress("USELESS_ELVIS")
    override fun updateDataFrom(new: Channel?): Channel {
        val c = new ?: return this

        return Channel(
                c.id ?: id,
                c.type ?: type,
                c.guildId ?: guildId,
                c.position ?: position,
                c.permissionOverwrites ?: permissionOverwrites,
                c.name ?: name,
                c.topic ?: topic,
                c.nsfw ?: nsfw,
                c.lastMessageId ?: lastMessageId,
                c.bitrate ?: bitrate,
                c.userLimit ?: userLimit,
                c.rateLimitPerUser ?: rateLimitPerUser,
                c.recipients ?: recipients,
                c.icon ?: icon,
                c.ownerId ?: ownerId,
                c.applicationId ?: applicationId,
                c.parentId ?: parentId,
                c.lastPinTimestamp ?: lastPinTimestamp
        ).savePrev()
    }

    override val prevVersions: MutableList<Channel> = mutableListOf()

    val isText: Boolean
        get() = type.isText

    val isVoice: Boolean
        get() = type.isVoice

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun MessageId.message(): Message {
        return bot.getMessage(this@Channel, this)
    }
}
