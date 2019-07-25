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
        @SerializedName("flags") private val _flags: Int?,
        @SerializedName("premium_type") val premiumType: PremiumType?,
        val member: GuildMember? // from Message.mentions, maybe
) : Storable {
//    val userFlags: List<UserFlag>
//        get() = UserFlag[_flags].also(::println)
}

enum class UserFlag {
    None,
    DiscordEmployee,
    DiscordPartner,
    HypeSquadEvents,
    BugHunter,
    HouseBravery,
    HouseBrilliance,
    HouseBalance,
    EarlySupporter,
    TeamUser;

    companion object {
        // TODO
        operator fun get(flags: Int?): List<UserFlag> {
            println(flags)
            val list: MutableList<UserFlag> = mutableListOf()
            for (i in 0..10) {

            }

            return list.takeUnless { it.isEmpty() } ?: listOf(None)
        }
    }
}

enum class PremiumType {
    @SerializedName("1")
    NitroClassic,
    @SerializedName("2")
    Nitro,
}