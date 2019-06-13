package lib.model

import com.google.gson.annotations.SerializedName

@Suppress("ArrayInDataClass")
data class GuildMember(
        val user: User,
        val nick: String?,
        val roles: Array<Snowflake>,
        @SerializedName("joined_at") val joinedAt: Timestamp,
        @SerializedName("premium_since") val premiumSince: Timestamp?,
        val deaf: Boolean,
        val mute: Boolean
)