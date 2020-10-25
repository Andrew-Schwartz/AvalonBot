package lib.model.guild

import com.google.gson.annotations.SerializedName
import lib.model.GuildId
import lib.model.RoleId
import lib.model.Timestamp
import lib.model.user.User

@Suppress("ArrayInDataClass")
data class GuildMember(
        /**
         * Partial user object. Contains:
         *
         * id
         *
         * username
         *
         * avatar
         *
         * discriminator
         *
         * publicFlags
         */
        val user: User,
        val nick: String?,
        @SerializedName("roles") private val _roles: Array<String>,
        @SerializedName("joined_at") val joinedAt: Timestamp,
        @SerializedName("premium_since") val premiumSince: Timestamp?,
        val deaf: Boolean,
        val mute: Boolean,
        @SerializedName("guild_id") val guildId: GuildId?, // for GuildMemberAdd
) {
    val username: String = user.username

    val roles by lazy { _roles.map(::RoleId).also(::println) }
}