package lib.model

import com.google.gson.annotations.SerializedName

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
        @SerializedName("user_limit") val userLimit: Int?,
        @SerializedName("rate_limit_per_user") val rateLimitPerUser: Int?,
        val recipients: Array<User>?,
        val icon: String?,
        @SerializedName("owner_id") val ownerId: Snowflake?,
        @SerializedName("application_id") val applicationId: Snowflake?,
        @SerializedName("parent_id") val parentId: Snowflake?,
        @SerializedName("last_pin_timestamp") val lastPinTimestamp: Timestamp?
) : Storable

enum class ChannelType {
    @SerializedName("0")
    GuildText,
    @SerializedName("1")
    DM,
    @SerializedName("2")
    GuildVoice,
    @SerializedName("3")
    GroupDM,
    @SerializedName("4")
    GuildCategory,
    @SerializedName("5")
    GuildNews,
    @SerializedName("6")
    GuildStore;

    val isTextChannel: Boolean
        get() = this == GuildText || this == DM || this == GroupDM
}

data class Overwrite(
        val id: Snowflake,
        val type: String,
        val allow: Int, // bit set
        val deny: Int // bit set
)