package lib.rest.model.events.sendEvents

import com.google.gson.annotations.SerializedName
import lib.model.Activity
import lib.model.Snowflake
import lib.model.Timestamp
import lib.model.guild.ClientStatus
import lib.model.user.User
import lib.rest.model.GatewayOpcode

data class PresenceUpdate(
        val user: User,
        val roles: ArrayList<Snowflake>,
        val game: Activity? = null,
        @SerializedName("guild_id") val guildId: Snowflake,
        val status: Status,
        val activities: ArrayList<Activity>,
        @SerializedName("client_status") val clientStatus: ClientStatus,
        @SerializedName("premium_since") val premiumSince: Timestamp? = null,
        val nick: String? = null
) : SendEvent {
    override val opcode: GatewayOpcode = GatewayOpcode.StatusUpdate
}