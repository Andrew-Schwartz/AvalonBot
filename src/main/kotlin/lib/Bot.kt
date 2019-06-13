package lib

import avalonBot.api
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import lib.misc.fromJson
import lib.misc.toJson
import lib.misc.toJsonTree
import lib.model.User
import lib.rest.model.BotGateway
import lib.rest.model.GatewayPayload
import lib.rest.model.events.Heartbeat
import lib.rest.model.events.SendEvent
import lib.rest.model.events.sendEvents.ConnectionProperties
import lib.rest.model.events.sendEvents.Identify

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class Bot internal constructor(private val token: String) {
    private val authHeader = "Authorization" to "Bot $token"

    private val client = HttpClient(CIO).config {
        install(WebSockets)
    }

    lateinit var sendWebsocket: suspend (String) -> Unit

    var heartbeatJob: Job? = null
    var sequenceNum: Int? = null

    init {
        runBlocking {
            client.wss(host = getGateway(), port = 443) {
                sendWebsocket = { send(it) }

                launch {
                    println("closed because ${this@wss.closeReason.await()}")
                }

                while (!incoming.isClosedForReceive) {
                    val message = try {
                        incoming.receive()
                    } catch (e: ClosedReceiveChannelException) {
                        println("WSS channel closed: ${e.message}")
                        break
                    }

                    val payload: GatewayPayload = (message as Frame.Text).readText().fromJson()

                    receive(payload)
                }
            }
        }

        client.close()
    }

    private suspend fun receive(payload: GatewayPayload) {
//        when (payload.opcode) {
        when (payload.op) {
            10 -> {
                initializeConnection(payload)
            }
            0 -> {
                sequenceNum = payload.sequenceNumber
                println("dispatch: $payload")
            }
            1 -> {
//                sendGateway(GatewayOpcode.HeartbeatAck)
            }
            11 -> {
                // nothing to do afaik
            }
            else -> {
                TODO("${payload.op} not yet implemented")
            }
        }
    }

    private suspend fun initializeConnection(payload: GatewayPayload) {
        val identify = Identify(token, ConnectionProperties())
        sendGateway(identify)
//        sendGateway(GatewayOpcode.Identify, identify.toJsonTree())

        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                sequenceNum?.let {
                    sendGateway(Heartbeat(it))
                    delay(payload.eventData!!.asJsonObject["heartbeat_interval"].asLong)
                }
//                if (sequenceNum != null) {
//                    sendGateway(GatewayOpcode.Heartbeat, JsonPrimitive(sequenceNum))
//                }
            }
        }
    }

    private suspend fun sendGateway(payload: SendEvent) {
        println("sending ${payload.opcode}")
        val message = GatewayPayload(payload.opcode, payload.toJsonTree(), sequenceNum, "")
        sendWebsocket(message.toJson())
    }

    @KtorExperimentalAPI
    suspend fun getRequest(url: String): HttpResponse {
        return client.get("$api$url") { header(authHeader.first, authHeader.second) }
    }

    @KtorExperimentalAPI
    suspend fun getUser(id: String = "@me"): User = getRequest("/users/$id").fromJson()

    @KtorExperimentalAPI
    suspend fun getGateway(): String = getRequest("/gateway/bot").fromJson<BotGateway>().url.removePrefix("wss://")
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun bot(token: String, block: /*suspend*/ Bot.() -> Unit) {
    val bot = Bot(token)
    bot.apply(block)
}