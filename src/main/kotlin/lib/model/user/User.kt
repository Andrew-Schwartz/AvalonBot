package lib.model.user

import com.google.gson.annotations.SerializedName
import lib.model.Snowflake
import lib.model.Storable
import lib.model.guild.GuildMember
import lib.util.L

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
    @Suppress("USELESS_ELVIS")
    override fun updateDataFrom(new: Storable?): User {
        val u = (new as? User) ?: throw IllegalArgumentException("Can only copy info from other users")

        return User(
                u.id ?: id,
                u.username ?: username,
                u.discriminator ?: discriminator,
                u.avatar ?: avatar,
                u.isBot ?: isBot,
                u.mfaEnabled ?: mfaEnabled,
                u.locale ?: locale,
                u.verified ?: verified,
                u.email ?: email,
                u._flags ?: _flags,
                u.premiumType ?: premiumType,
                u.member ?: member
        )
    }

    override fun equals(other: Any?): Boolean = (other as? User)?.id == id

    override fun hashCode(): Int = id.hashCode()

    val userFlags: List<UserFlag>
        get() = UserFlag.get(flags = _flags)
}

enum class UserFlag(val mask: Int) {
    None(0),
    DiscordEmployee(1),
    DiscordPartner(2),
    HypeSquadEvents(4),
    BugHunter(8),
    HouseBravery(64),
    HouseBrilliance(128),
    HouseBalance(256),
    EarlySupporter(512),
    TeamUser(1024);

    companion object {
        operator fun invoke(mask: Int): UserFlag? = values().firstOrNull { it.mask == mask }

        fun get(flags: Int?): List<UserFlag> {
            println("flags value: $flags")

            return ((0..3) + (6..10))
                    .asSequence()
                    .map { 1 shl it }
                    .filter { mask -> flags?.and(mask) ?: 0 != 0 }
                    .map { mask -> invoke(mask)!! }
                    .toList()
                    .takeUnless { it.isEmpty() }
                    ?: L[None]

//            val flags = flags ?: return L[None]
//            val list: MutableList<UserFlag> = mutableListOf()
//            for (i in (0..3) + (6..10)) {
//                val mask = 1 shl i
//                if (flags and mask != 0) {
//                    list += invoke(mask)!!
//                }
//            }
//
//            return list.takeUnless { it.isEmpty() } ?: L[None]
        }
    }
}
