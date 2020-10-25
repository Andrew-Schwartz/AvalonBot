package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.model.*
import lib.model.user.User

@Suppress("ArrayInDataClass")
data class Channel(
        override val id: ChannelId,
        val type: ChannelType,
        @SerializedName("guild_id") val guildId: GuildId?,
        var position: Int?,
        @SerializedName("permission_overwrites") var permissionOverwrites: Array<Overwrite>,
        var name: String?,
        var topic: String?,
        var nsfw: Boolean?,
        @SerializedName("last_message_id") var lastMessageId: MessageId?,
        var bitrate: Int?,
        @SerializedName("userLimit") var userLimit: Int?,
        @SerializedName("rate_limit_per_user") var rateLimitPerUser: Int?,
        var recipients: Array<User>?,
        var icon: String?,
        @SerializedName("owner_id") var ownerId: UserId?,
        @SerializedName("application_id") var applicationId: ApplicationId?,
        @SerializedName("parent_id") var parentId: ChannelId?,
        @SerializedName("last_pin_timestamp") var lastPinTimestamp: Timestamp?,
) : Storable<Channel>, IntoId<ChannelId> by id {
    override fun equals(other: Any?): Boolean = (other as? Channel)?.id == id

    override fun hashCode(): Int = id.hashCode()

    @Suppress("USELESS_ELVIS")
    override fun updateFrom(new: Channel) {
        position = new.position ?: position
        permissionOverwrites = new.permissionOverwrites ?: permissionOverwrites
        name = new.name ?: name
        topic = new.topic ?: topic
        nsfw = new.nsfw ?: nsfw
        lastMessageId = new.lastMessageId ?: lastMessageId
        bitrate = new.bitrate ?: bitrate
        userLimit = new.userLimit ?: userLimit
        rateLimitPerUser = new.rateLimitPerUser ?: rateLimitPerUser
        recipients = new.recipients ?: recipients
        icon = new.icon ?: icon
        ownerId = new.ownerId ?: ownerId
        applicationId = new.applicationId ?: applicationId
        parentId = new.parentId ?: parentId
        lastPinTimestamp = new.lastPinTimestamp ?: lastPinTimestamp
    }

    val isDM: Boolean
        get() = type.isDM

    val isText: Boolean
        get() = type.isText

    val isVoice: Boolean
        get() = type.isVoice
}