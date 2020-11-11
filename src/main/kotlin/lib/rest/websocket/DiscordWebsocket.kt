package lib.rest.websocket

import common.util.now
import common.util.onNull
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*
import lib.dsl.Bot
import lib.dsl.on
import lib.exceptions.InvalidSessionException
import lib.rest.client
import lib.rest.http.httpRequests.gateway
import lib.rest.model.GatewayOpcode
import lib.rest.model.GatewayPayload
import lib.rest.model.events.receiveEvents.*
import lib.rest.model.events.sendEvents.*
import lib.util.fromJson
import lib.util.toJson
import lib.util.toJsonTree
import java.time.Instant
import java.time.OffsetDateTime

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class DiscordWebsocket(private val token: String) {
    private lateinit var sendWebsocket: suspend (String) -> Unit
    lateinit var close: suspend (CloseReason.Codes, message: String) -> Unit

    private var heartbeatJob: Job? = null
    private var sequenceNumber: Int? = null
    private var sessionId: String? = null
    internal var authed = false

    private var lastAck: Instant? = null
    private var lastHeartbeat: Instant? = null
    private var strikes: Int = 0

    suspend fun run(): Nothing {
        defaultListeners()
        while (true) {
            println("[${now()}] Starting websocket")
            runCatching {
                client.wss(host = gateway(), port = 443) {
                    // set up callbacks to interact with ws from other functions
                    sendWebsocket = { send(it) }
                    close = { code, message -> close(CloseReason(code, message)) }

                    // Resume on reconnect
                    if (sessionId != null) {
                        val resume = Resume(token, sessionId!!, sequenceNumber!!)
                        println("[${now()}] Sending Resume...")
                        sendGatewayEvent(resume)
                    }

                    // diagnostic
                    launch {
                        val closeReason = closeReason.await()
                        println("[${now()}] closed because $closeReason")
                    }

                    eventLoop@ while (!incoming.isClosedForReceive) {
                        val message = incoming.receive() as Frame.Text

                        runCatching { receive(message.readText().fromJson()) }
                                .onFailure {
                                    it.printStackTrace()
                                    if (it is InvalidSessionException) throw it
                                }
                    }
                    close(CloseReason.Codes.GOING_AWAY, "Incoming is closed")
                }
            }.onFailure {
                lastAck = null
                lastHeartbeat = null
                strikes = 0
                println("[${now()}] caught $it: ${it.message}")
            }
        }
    }

    private suspend fun receive(payload: GatewayPayload) {
        when (payload.opcode) {
            GatewayOpcode.Hello -> {
                initializeConnection(payload)
            }
            GatewayOpcode.Dispatch -> {
                processDispatch(payload)
            }
            GatewayOpcode.Heartbeat -> {
                println("recv: Heartbeat")
            }
            GatewayOpcode.HeartbeatAck -> {
                lastAck = Instant.now()
            }
            GatewayOpcode.Reconnect -> {
                println("[${now()}] recv: Reconnect")
                close(CloseReason.Codes.SERVICE_RESTART, "Reconnect requested by Discord")
            }
            GatewayOpcode.InvalidSession -> {
                println("[${now()}] recv Invalid Session: $payload")
                val resumable = payload.eventData!!.asBoolean
                if (!resumable) {
                    resetConnectionState()
                    throw InvalidSessionException("Non Resumable session")
                }
            }
            else -> {
                println("[${now()}] should not receive ${payload.opcode}, it's content was ${payload.eventData}")
            }
        }
    }

    private fun resetConnectionState() {
        authed = false
        sequenceNumber = null
        sessionId = null
    }

    /**
     * Statically known that [gatewayPayload]'s payload is a [DispatchEvent] at this point.
     *
     * Depending on it's name, Deserialize it to the correct type and run all applicable actions
     */
    private suspend fun processDispatch(gatewayPayload: GatewayPayload) {
        sequenceNumber = gatewayPayload.sequenceNumber

        // Not null since these are guaranteed to be in dispatches
        val payload = gatewayPayload.eventData!!
        val name = gatewayPayload.eventName!!.replace("_", "")

        val kClass = DispatchEvent::class.sealedSubclasses
                .firstOrNull { it.simpleName?.toUpperCase() == name }
                .onNull { println("[${now()}] No DispatchEvent for $name, data is:\n$payload") }
                ?: return

        val event: DispatchEvent<*> = kClass.objectInstance!!
        event.runActions(payload)
    }

    private suspend fun initializeConnection(payload: GatewayPayload) {
        if (!authed) {
            val identify = Identify(
                    token,
                    ConnectionProperties(),
                    intents = Intent.sendBits(),
            )
            sendGatewayEvent(identify)
            authed = true
        }
        val delayTime = payload.eventData!!.asJsonObject["heartbeat_interval"].asLong

        heartbeatJob?.cancel()
        heartbeatJob = Bot.launch {
            while (isActive) {
                sequenceNumber?.let {
                    if (lastHeartbeat != null && lastAck != null && lastHeartbeat!!.isAfter(lastAck!!)) {
                        strikes++
                        println("[${now()}] ACK Strike $strikes")
                        if (strikes >= 3) {
                            resetConnectionState()
                            close(CloseReason.Codes.SERVICE_RESTART, "ACK not recent enough, closing websocket")
                        }
                    }
                    sendGatewayEvent(Heartbeat(it))
                    lastHeartbeat = Instant.now()
                    delay(delayTime)
                }
            }
        }
    }

    /**
     * Sends [payload] to Discord's Websocket
     */
    internal suspend fun sendGatewayEvent(payload: SendEvent) {
        val message = GatewayPayload(payload.opcode.code, payload.toJsonTree())
        val json = message.toJson()
        if (payload !is Heartbeat) {
            println("[${now()}] send: $json")
        }
        sendWebsocket(json)
    }

    /**
     * Load up important listeners on various [DispatchEvent]s.
     */
    private fun defaultListeners() {
        with(Bot) {
            // Initialize the bot's user object and websocket data before anything other Ready actions
            Ready.actions.add(0) {
                Bot.user = this.user
                this@DiscordWebsocket.sessionId = sessionId
                logInTime = OffsetDateTime.now()
                if (firstLogInTime == null) {
                    firstLogInTime = logInTime
                }
            }
            // trigger the derived MessageReactionUpdate when either MRAdd or MRRemove happen
            on(MessageReactionAdd) {
                MessageReactionUpdate.actions.forEach {
                    toUpdate().it()
                }
            }
            on(MessageReactionRemove) {
                MessageReactionUpdate.actions.forEach {
                    toUpdate().it()
                }
            }
            on(ChannelCreate) {
                channels.addOrUpdate(this)
            }
            on(GuildCreate) {
                println("[${now()}] Guild ${this.name} created! (${this.id})")
            }
        }
    }
}