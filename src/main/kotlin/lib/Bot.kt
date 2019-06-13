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
import lib.model.*
import lib.rest.model.BotGateway
import lib.rest.model.GatewayOpcode.*
import lib.rest.model.GatewayPayload
import lib.rest.model.events.SendEvent
import lib.rest.model.events.receiveEvents.*
import lib.rest.model.events.receiveEvents.DispatchEvent.*
import lib.rest.model.events.sendEvents.ConnectionProperties
import lib.rest.model.events.sendEvents.Heartbeat
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
    var sequenceNumber: Int? = null

    var botUser: User? = null
    var sessionId: String? = null

    private val guilds: MutableMap<Snowflake, Guild> = hashMapOf()
    private val channels: MutableMap<Snowflake, Channel> = hashMapOf()
    private val messages: MutableMap<Snowflake, Message> = hashMapOf()

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

                    receive((message as Frame.Text).readText().fromJson())
                }
            }
        }

        client.close()
    }

    private suspend fun receive(payload: GatewayPayload) {
        when (payload.opcode) {
            Hello -> initializeConnection(payload)
            Dispatch -> processDispatch(payload)
            Heartbeat, HeartbeatAck -> {
                // nothing to do on heartbeat
            }
            Reconnect -> {
                TODO("implement Reconnect")
            }
            InvalidSession -> {
                TODO("implement InvalidSession")
            }
            else -> {
                throw IllegalStateException("${payload.opcode} should not be received")
            }
        }
    }

    private fun processDispatch(payload: GatewayPayload) {
        sequenceNumber = payload.sequenceNumber

        // Not null since these are sent in dispatches
        val data = payload.eventData!!

        val name = payload.eventName!!

        when (val event = DispatchEvent.values().first { it.eventName == name }) {
            Ready -> {
                val ready: ReadyEvent = data.fromJson()
                botUser = ready.user
                sessionId = ready.sessionId

                println("${botUser!!.username} is ready!")
            }
            ChannelCreate -> {
                val channel: Channel = data.fromJson()
                channels += channel.id to channel
            }
            ChannelUpdate -> {
                val channel: Channel = data.fromJson()
                channels[channel.id] = channel
            }
            ChannelDelete -> {
                val channel: Channel = data.fromJson()
                channels -= channel.id
            }
            ChannelPinsUpdate -> {
                val pinInfo: PinsUpdate = data.fromJson()
                // do something presumably
            }
            GuildCreate -> {
                val guild: Guild = data.fromJson()
                guilds += guild.id to guild
            }
            GuildUpdate -> {
                val guild: Guild = data.fromJson()
                guilds[guild.id] = guild
            }
            GuildDelete -> {
                val guild: Guild = data.fromJson()
                guilds -= guild.id
            }
            GuildBanAdd -> {
                val banChangeInfo: GuildBanUpdate = data.fromJson()
                // do something presumably
            }
            GuildBanRemove -> {
                val banChangeInfo: GuildBanUpdate = data.fromJson()
                // do something presumably
            }
            GuildEmojisUpdate -> {
                val emojisEvent: GuildEmojisEvent = data.fromJson()
                // do something presumably
            }
            MessageCreate -> {
                val message: Message = data.fromJson()
                messages += message.id to message
            }
            MessageUpdate -> {
                val message: Message = data.fromJson()
                messages[message.id] = message
            }
            MessageDelete -> {
                val deleteInfo: MessageDeleteEvent = data.fromJson()
                messages -= deleteInfo.id
            }
            MessageDeleteBulk -> {
                val deleteInfo: MessageDeleteBulkEvent = data.fromJson()
                for (id in deleteInfo.ids) {
                    messages -= id
                }
            }
            MessageReactionAdd -> {
                val reactionUpdate: MessageReactionUpdate = data.fromJson()
            }
            MessageReactionRemove -> {
                val reactionUpdate: MessageReactionUpdate = data.fromJson()
            }
            MessageReactionRemoveAll -> {
                val reactionUpdate: MessageReactionRemoveAllEvent = data.fromJson()
            }
            TypingStart -> {

            }
            UserUpdate -> {
                val user: User = data.fromJson()
//                users[user.id] = user
            }
            else -> {
                println("$event is unhandled")
            }
        }
    }

    private suspend fun initializeConnection(payload: GatewayPayload) {
        sendGateway(Identify(token, ConnectionProperties()))

        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                sequenceNumber?.let {
                    sendGateway(Heartbeat(it))
                    delay(payload.eventData!!.asJsonObject["heartbeat_interval"].asLong)
                }
            }
        }
    }

    private suspend fun sendGateway(payload: SendEvent) {
//        println("sending ${payload.opcode}")
        val message = GatewayPayload(payload.opcode.code, payload.toJsonTree(), sequenceNumber)
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