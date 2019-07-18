package lib.model

import com.google.gson.annotations.SerializedName

@Suppress("ArrayInDataClass")
data class GuildMember(
        val user: User,
        val nick: String?,
        @SerializedName("roles") private val _roles: Array<String>,
        @SerializedName("joined_at") val joinedAt: Timestamp,
        @SerializedName("premium_since") val premiumSince: Timestamp?,
        val deaf: Boolean,
        val mute: Boolean,
        @SerializedName("guild_id") val guildId: Snowflake? // for GuildMemberAdd
) {
    val username: String = user.username

    val roles: Array<Snowflake> by lazy { _roles.map { Snowflake(it) }.toTypedArray() }
}