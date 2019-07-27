package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.model.Snowflake
import lib.model.Storable
import lib.model.Timestamp
import lib.model.user.User

@Suppress("ArrayInDataClass")
data class Channel(
        override val id: Snowflake,
        val type: ChannelType,
        @SerializedName("guild_id") val guildId: Snowflake?,
        val position: Int?,
        @SerializedName("permission_overwrites") val permissionOverwrites: Array<Overwrite>,
        val name: String?,
        val topic: String?,
        val nsfw: Boolean?,
        @SerializedName("last_message_id") val lastMessageId: Snowflake?,
        val bitrate: Int?,
        @SerializedName("userLimit") val userLimit: Int?,
        @SerializedName("rate_limit_per_user") val rateLimitPerUser: Int?,
        val recipients: Array<User>?,
        val icon: String?,
        @SerializedName("owner_id") val ownerId: Snowflake?,
        @SerializedName("application_id") val applicationId: Snowflake?,
        @SerializedName("parent_id") val parentId: Snowflake?,
        @SerializedName("last_pin_timestamp") val lastPinTimestamp: Timestamp?
) : Storable {
    val isText: Boolean
        get() = type.isText

    val isVoice: Boolean
        get() = type.isVoice
}
