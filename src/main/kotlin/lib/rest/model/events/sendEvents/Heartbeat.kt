package lib.rest.model.events.sendEvents

import lib.rest.model.GatewayOpcode

data class Heartbeat(
        val sequenceNumber: Int,
) : SendEvent {
    override val opcode: GatewayOpcode
        get() = GatewayOpcode.Heartbeat
}