package lib.rest.model.events.sendEvents

import lib.rest.model.GatewayOpcode
import lib.rest.model.events.SendEvent

data class Heartbeat(
        val sequenceNumber: Int
) : SendEvent {
    override val opcode: GatewayOpcode
        get() = GatewayOpcode.Heartbeat
}