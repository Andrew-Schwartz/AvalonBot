package lib.rest.model.events.sendEvents

import com.google.gson.annotations.SerializedName
import common.util.A
import lib.model.Activity
import lib.rest.model.GatewayOpcode

@Suppress("ArrayInDataClass")
data class Identify(
        val token: String,
        val properties: ConnectionProperties,
        val compress: Boolean = false,
        @SerializedName("large_threshold") val largeThreshold: Int = 50,
        val shard: Array<Int>? = A[0, 1],
        val presence: GatewayStatus? = null,
        @SerializedName("guild_subscriptions") val guildSubscriptions: Boolean = true,
        val intents: Int? = null
) : SendEvent {
    override val opcode: GatewayOpcode
        get() = GatewayOpcode.Identify
}

data class ConnectionProperties(
        val `$os`: String = "windows",
        val `$browser`: String = "AvBot",
        val `$device`: String = "AvBot"
)

data class GatewayStatus(
        val since: Int?,
        val game: Activity?,
        val status: Status,
        val afk: Boolean
)

enum class Status {
    @SerializedName("online")
    Online,
    @SerializedName("dnd")
    DoNotDisturb,
    @SerializedName("idle")
    AFK,
    @SerializedName("invisible")
    Invisible,
    @SerializedName("offline")
    Offline
}