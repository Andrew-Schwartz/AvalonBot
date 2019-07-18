package lib.model

import com.google.gson.annotations.SerializedName

data class User(
        override val id: Snowflake,
        val username: String,
        val discriminator: String,
        val avatar: String?,
        @SerializedName("bot") val isBot: Boolean?,
        @SerializedName("mfa_enabled") val mfaEnabled: Boolean?,
        val locale: String?,
        val verified: Boolean?,
        val email: String?,
        val flags: Int?,
        @SerializedName("premium_type") val premiumType: PremiumType?,
        val member: GuildMember? // from Message.mentions, maybe
) : Storable

enum class PremiumType {
    @SerializedName("1")
    NitroClassic,
    @SerializedName("2")
    Nitro,
}