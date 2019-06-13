package lib.rest.model

import com.google.gson.annotations.SerializedName
import lib.model.Activity

@Suppress("ArrayInDataClass")
data class Identify(
        val token: String,
        val properties: ConnectionProperties,
        @SerializedName("large_threshold") val largeThreshold: Int = 50,
        val compress: Boolean = false,
        val shard: Array<Int>? = arrayOf(0, 1),
        val presence: GatewayStatus? = null
)

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