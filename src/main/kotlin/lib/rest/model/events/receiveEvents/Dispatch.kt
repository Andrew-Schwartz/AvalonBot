package lib.rest.model.events.receiveEvents

import lib.rest.model.events.ReceiveEvent

data class Dispatch(
        val event:
) : ReceiveEvent {
    override val opcode: Int
        get() = 0
}