package lib.rest.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class GatewayPayload(
        val op: Int,
        @SerializedName("d") val eventData: JsonElement? = null,
        @SerializedName("s") val sequenceNumber: Int? = null,
        @SerializedName("t") val eventName: String? = null
) {
    constructor(
            opcode: GatewayOpcode,
            eventData: JsonElement? = null,
            sequenceNumber: Int? = null,
            eventName: String? = null
    ) : this(opcode.code, eventData, sequenceNumber, eventName)

    val opcode get() = GatewayOpcode.values().first { it.code == op }
}

enum class GatewayOpcode(val code: Int) {
    @SerializedName("0")
    Dispatch(0),
    @SerializedName("1")
    Heartbeat(1),
    @SerializedName("2")
    Identify(2),
    @SerializedName("3")
    StatusUpdate(3),
    @SerializedName("4")
    VoiceStateUpdate(4),
    @SerializedName("6")
    Resume(6),
    @SerializedName("7")
    Reconnect(7),
    @SerializedName("8")
    RequestGuildMembers(8),
    @SerializedName("9")
    InvalidSession(9),
    @SerializedName("10")
    Hello(10),
    @SerializedName("11")
    HeartbeatAck(11)
}