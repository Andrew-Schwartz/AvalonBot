package lib.rest.model.events.sendEvents

import com.google.gson.annotations.SerializedName
import lib.model.Activity
import lib.model.GuildId
import lib.model.RoleId
import lib.model.Timestamp
import lib.model.guild.ClientStatus
import lib.model.user.User
import lib.rest.model.GatewayOpcode

data class PresenceUpdate(
        val user: User,
        val roles: ArrayList<RoleId>,
        val game: Activity? = null,
        @SerializedName("guild_id") val guildId: GuildId,
        val status: Status,
        val activities: ArrayList<Activity>,
        @SerializedName("client_status") val clientStatus: ClientStatus,
        @SerializedName("premium_since") val premiumSince: Timestamp? = null,
        val nick: String? = null
) : SendEvent {
    override val opcode: GatewayOpcode = GatewayOpcode.StatusUpdate
}