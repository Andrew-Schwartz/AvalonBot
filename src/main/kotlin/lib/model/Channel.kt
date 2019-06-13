package lib.model

import com.google.gson.annotations.SerializedName

@Suppress("ArrayInDataClass")
data class Channel(
        val id: Snowflake,
        val type: Int,
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
)

data class Overwrite(
        val id: Snowflake,
        val type: String,
        val allow: Int, // bit sets
        val deny: Int // bit sets
)