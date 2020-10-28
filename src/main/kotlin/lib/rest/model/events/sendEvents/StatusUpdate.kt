package lib.rest.model.events.sendEvents

import com.google.gson.annotations.SerializedName
import lib.model.Activity
import lib.rest.model.GatewayOpcode

/**
 * Sent by the client to indicate a presence or status update.
 */
data class StatusUpdate(
        val activities: List<Activity>,
        val status: Status,
        val afk: Boolean,
        /**
         * unix time (in milliseconds) of when the client went idle, or null if the client is not idle
         */
        @SerializedName("since") val idleSince: Long,
) : SendEvent {
    override val opcode: GatewayOpcode
        get() = GatewayOpcode.UpdateStatus
}

enum class Status {
    @SerializedName("online")
    Online,

    @SerializedName("dnd")
    DoNotDisturb,

    @SerializedName("idle")
    Idle,

    @SerializedName("invisible")
    Invisible,

    @SerializedName("offline")
    Offline
}