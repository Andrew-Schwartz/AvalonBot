package lib.rest.websocket

import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import lib.Bot
import lib.dsl.messageCreateEvents
import lib.dsl.readyEvents
import lib.misc.fromJson
import lib.misc.toJson
import lib.misc.toJsonTree
import lib.model.Channel
import lib.model.Guild
import lib.model.Message
import lib.model.User
import lib.rest.client
import lib.rest.http.getGateway
import lib.rest.model.GatewayOpcode
import lib.rest.model.GatewayPayload
import lib.rest.model.events.receiveEvents.*
import lib.rest.model.events.sendEvents.ConnectionProperties
import lib.rest.model.events.sendEvents.Heartbeat
import lib.rest.model.events.sendEvents.Identify
import lib.rest.model.events.sendEvents.SendEvent

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class DiscordWebsocket(val bot: Bot) {
    lateinit var sendWebsocket: suspend (String) -> Unit

    var heartbeatJob: Job? = null
    var sequenceNumber: Int? = null

    suspend fun run() {
        client.wss(host = bot.getGateway(), port = 443) {
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

        client.close()
    }

    private suspend fun receive(payload: GatewayPayload) = when (payload.opcode) {
        GatewayOpcode.Hello -> {
            initializeConnection(payload)
        }
        GatewayOpcode.Dispatch -> {
            processDispatch(payload)
        }
        GatewayOpcode.Heartbeat, GatewayOpcode.HeartbeatAck -> {
            // nothing to do on heartbeat
        }
        GatewayOpcode.Reconnect -> {
            TODO("implement Reconnect")
        }
        GatewayOpcode.InvalidSession -> {
            TODO("implement InvalidSession")
        }
        else -> {
            throw IllegalStateException("${payload.opcode} should not be received")
        }
    }

    private suspend fun processDispatch(payload: GatewayPayload) {
        sequenceNumber = payload.sequenceNumber

        // Not null since these are sent in dispatches
        val data = payload.eventData!!
        val name = payload.eventName!!

        val event = try {
            DispatchEvent.values().first { it.eventName == name }
        } catch (e: Exception) {
            println("no DispatchEvent for name $name")
            return
        }

        when (event) {
            DispatchEvent.Ready -> {
                val readyEvent: ReadyEvent = data.fromJson()
                bot.user = readyEvent.user
                bot.sessionId = readyEvent.sessionId

                readyEvents.forEach { it(readyEvent) }
            }
            DispatchEvent.ChannelCreate -> {
                val channel: Channel = data.fromJson()
                bot.channels += channel.id to channel
            }
            DispatchEvent.ChannelUpdate -> {
                val channel: Channel = data.fromJson()
                bot.channels[channel.id] = channel
            }
            DispatchEvent.ChannelDelete -> {
                val channel: Channel = data.fromJson()
                bot.channels -= channel.id
            }
            DispatchEvent.ChannelPinsUpdate -> {
                val pinInfo: PinsUpdate = data.fromJson()
                // do something presumably
            }
            DispatchEvent.GuildCreate -> {
                val guild: Guild = data.fromJson()
                bot.guilds += guild.id to guild
            }
            DispatchEvent.GuildUpdate -> {
                val guild: Guild = data.fromJson()
                bot.guilds[guild.id] = guild
            }
            DispatchEvent.GuildDelete -> {
                val guild: Guild = data.fromJson()
                bot.guilds -= guild.id
            }
            DispatchEvent.GuildBanAdd -> {
                val banChangeInfo: GuildBanUpdate = data.fromJson()
                // do something presumably
            }
            DispatchEvent.GuildBanRemove -> {
                val banChangeInfo: GuildBanUpdate = data.fromJson()
                // do something presumably
            }
            DispatchEvent.GuildEmojisUpdate -> {
                val emojisEvent: GuildEmojisEvent = data.fromJson()
                // do something presumably
            }
            DispatchEvent.MessageCreate -> {
                val message: Message = data.fromJson()
                messageCreateEvents.forEach { it(message) }
                bot.messages += message.id to message
            }
            DispatchEvent.MessageUpdate -> {
                val message: Message = data.fromJson()
                bot.messages[message.id] = message
            }
            DispatchEvent.MessageDelete -> {
                val deleteInfo: MessageDeleteEvent = data.fromJson()
                bot.messages -= deleteInfo.id
            }
            DispatchEvent.MessageDeleteBulk -> {
                val deleteInfo: MessageDeleteBulkEvent = data.fromJson()
                for (id in deleteInfo.ids) {
                    bot.messages -= id
                }
            }
            DispatchEvent.MessageReactionAdd -> {
                val reactionUpdate: MessageReactionUpdate = data.fromJson()
            }
            DispatchEvent.MessageReactionRemove -> {
                val reactionUpdate: MessageReactionUpdate = data.fromJson()
            }
            DispatchEvent.MessageReactionRemoveAll -> {
                val reactionUpdate: MessageReactionRemoveAllEvent = data.fromJson()
            }
            DispatchEvent.TypingStart -> {

            }
            DispatchEvent.UserUpdate -> {
                val user: User = data.fromJson()
//                users[user.id] = user
            }
            else -> {
                println("$event is unhandled")
            }
        }
    }

    private suspend fun initializeConnection(payload: GatewayPayload) {
        sendGateway(Identify(bot.token, ConnectionProperties()))

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
}