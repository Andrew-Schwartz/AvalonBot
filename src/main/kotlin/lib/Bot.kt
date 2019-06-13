package lib

import avalonBot.api
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
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
import lib.model.Guild
import lib.model.User
import lib.rest.model.*

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class Bot internal constructor(private val token: String) {
    companion object {
        private val authHeader = "Authorization" to "Bot ${avalonBot.token}"
    }

    private val client = HttpClient(CIO).config {
        install(WebSockets)
    }

    lateinit var sendWebsocket: suspend (String) -> Unit

    var heartbeatJob: Job? = null
    var sequenceNum: Int? = null

    init {
        runBlocking {
            client.wss(host = getGatewayUrl(), port = 443) {
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

//                    when (message) {
//                        is Frame.Text -> {
//                            val payload: GatewayPayload = message.readText().fromJson()
//
//                            receive(payload)
//                        }
//                        is Frame.Close -> {
//                            println("Frame Closing")
//                        }
//                    }
                }
            }
        }

        client.close()
    }

    private suspend fun receive(payload: GatewayPayload) {
        when (payload.opcode) {
            GatewayOpcode.Hello -> {
                val identify = Identify(avalonBot.token, ConnectionProperties())
                sendGateway(GatewayOpcode.Identify, identify)

                heartbeatJob?.cancel()
                heartbeatJob = CoroutineScope(Dispatchers.Default).launch {
                    while (isActive) {
                        if (sequenceNum != null) {
                            sendGateway(GatewayOpcode.Heartbeat, JsonPrimitive(sequenceNum))
                            delay(payload.eventData!!.asJsonObject["heartbeat_interval"].asLong)
                        }
                    }
                }
            }
            GatewayOpcode.Heartbeat -> {
                sendGateway(GatewayOpcode.HeartbeatAck)
            }
            GatewayOpcode.Dispatch -> {
                sequenceNum = payload.sequenceNumber
                println("dispatch payload is $payload")
            }
            GatewayOpcode.HeartbeatAck -> {
                // nothing to do afaik
            }
            else -> {
                TODO("${payload.opcode} not yet implemented")
            }
        }
    }

    private suspend fun sendGateway(opcode: GatewayOpcode, payload: JsonElement? = null) {
        println("sending $opcode")
        val message = GatewayPayload(opcode, payload, sequenceNum, "")
        sendWebsocket(message.toJson())
    }

    private suspend inline fun <reified T> sendGateway(opcode: GatewayOpcode, payload: T? = null) {
        println("sending $opcode")
        val message = GatewayPayload(opcode, payload.toJsonTree(), sequenceNum, "")
        sendWebsocket(message.toJson())
    }

    @KtorExperimentalAPI
    suspend fun getRequest(url: String): HttpResponse {
        return client.get("$api$url") { header(authHeader.first, authHeader.second) }
    }

    @KtorExperimentalAPI
    suspend fun User.guilds(): Array<Guild> {
        val response = getRequest("/users/@me/guilds").fromJson<JsonArray>()

        return response.fromJson()
    }

    @KtorExperimentalAPI
    suspend fun getUser(id: String = "@me"): User = getRequest("/users/$id").fromJson()

    @KtorExperimentalAPI
    suspend fun getGateway(): BotGateway = getRequest("/gateway/bot").fromJson()

    @KtorExperimentalAPI
    suspend fun getGatewayUrl(): String = getGateway().url.removePrefix("wss://")
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun bot(token: String, block: /*suspend*/ Bot.() -> Unit) {
    val bot = Bot(token)
    bot.apply(block)
}