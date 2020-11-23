package lib.model.user

import com.google.gson.annotations.SerializedName
import common.util.L
import lib.model.IntoId
import lib.model.Storable
import lib.model.UserId

data class User(
        val id: UserId,
        var username: String,
        var discriminator: String,
        var avatar: String?,
        @SerializedName("bot") var isBot: Boolean?,
        @SerializedName("mfa_enabled") var mfaEnabled: Boolean?,
        var locale: String?,
        var verified: Boolean?,
        var email: String?,
        @SerializedName("flags") private var _flags: Int?,
        @SerializedName("premium_type") var premiumType: PremiumType?,
) : Storable<UserId, User>, IntoId<UserId> by id {
    @Suppress("USELESS_ELVIS")
    override fun updateFrom(new: User) {
        username = new.username ?: username
        discriminator = new.discriminator ?: discriminator
        avatar = new.avatar ?: avatar
        isBot = new.isBot ?: isBot
        mfaEnabled = new.mfaEnabled ?: mfaEnabled
        locale = new.locale ?: locale
        verified = new.verified ?: verified
        email = new.email ?: email
        _flags = new._flags ?: _flags
        premiumType = new.premiumType ?: premiumType
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
        }
    }
}