package lib.rest.model.events.sendEvents

import lib.rest.model.GatewayOpcode

interface SendEvent {
    val opcode: GatewayOpcode
}