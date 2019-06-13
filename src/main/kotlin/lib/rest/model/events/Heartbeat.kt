package lib.rest.model.events

data class Heartbeat(
        val sequenceNumber: Int
) : SendEvent, ReceiveEvent {
    override val opcode: Int
        get() = 1
}