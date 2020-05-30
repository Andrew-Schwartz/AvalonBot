package lib.rest.model.events.sendEvents

import com.google.gson.annotations.SerializedName
import lib.rest.model.GatewayOpcode

data class Resume(
        @SerializedName("d")
        val data: ResumeData
) : SendEvent {
    constructor(token: String, sessionId: String, seq: Int) : this(ResumeData(token, sessionId, seq))

    override val opcode: GatewayOpcode = GatewayOpcode.Resume
}

data class ResumeData(
        val token: String,
        @SerializedName("session_id") val sessionId: String,
        val seq: Int
)
