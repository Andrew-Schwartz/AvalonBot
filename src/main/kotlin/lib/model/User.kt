package lib.model

import com.google.gson.annotations.SerializedName

data class User(
        val id: Snowflake,
        val username: String,
        val discriminator: String,
        val avatar: String?,
        val bot: Boolean?,
        @SerializedName("mfa_enabled") val mfaEnabled: Boolean?,
        val locale: String?,
        val verified: Boolean?,
        val email: String?,
        val flags: Int?,
        @SerializedName("premium_type") val premiumType: Int?,
        val member: GuildMember? // from Message.mentions, maybe
)