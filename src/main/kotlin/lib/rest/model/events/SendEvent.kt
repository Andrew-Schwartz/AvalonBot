package lib.rest.model.events

import lib.rest.model.GatewayOpcode

interface SendEvent {
    val opcode: GatewayOpcode
}