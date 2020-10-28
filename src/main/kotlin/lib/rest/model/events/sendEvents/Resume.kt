package lib.rest.model.events.sendEvents

import com.google.gson.annotations.SerializedName
import lib.rest.model.GatewayOpcode

data class Resume(
        val token: String,
        @SerializedName("session_id") val sessionId: String,
        val seq: Int,
) : SendEvent {
    override val opcode: GatewayOpcode
        get() = GatewayOpcode.Resume
}
