package lib.rest.websocket

import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import lib.dsl.Bot
import lib.rest.client
import lib.rest.http.httpRequests.gateway
import lib.rest.model.GatewayOpcode
import lib.rest.model.GatewayPayload
import lib.rest.model.events.receiveEvents.*
import lib.rest.model.events.sendEvents.ConnectionProperties
import lib.rest.model.events.sendEvents.Heartbeat
import lib.rest.model.events.sendEvents.Identify
import lib.rest.model.events.sendEvents.SendEvent
import lib.util.fromJson
import lib.util.toJson
import lib.util.toJsonTree

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class DiscordWebsocket(val bot: Bot) {
    lateinit var sendWebsocket: suspend (String) -> Unit

    private var heartbeatJob: Job? = null
    private var sequenceNumber: Int? = null

    suspend fun run() {
        client.wss(host = bot.gateway(), port = 443) {
            sendWebsocket = { send(it) }

            launch {
                println("closed because ${this@wss.closeReason.await()}")
            }

            while (!incoming.isClosedForReceive) {
                try {
                    val message = incoming.receive()

                    receive((message as Frame.Text).readText().fromJson())
                } catch (e: ClosedReceiveChannelException) {
                    println("WSS getChannel closed: ${e.message}")
                    break
                } catch (e: ClientRequestException) {
                    e.printStackTrace()
                    break
                }
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
        val name = payload.eventName!!.replace("_", "")

        val event: DispatchEvent<*> = try {
            DispatchEvent::class.sealedSubclasses.first {
                val className = it.simpleName!!.toUpperCase() // none of these are anonymous
                className == name
            }.objectInstance!! // all dispatch events are objects
        } catch (e: NoSuchElementException) {
            println("no DispatchEvent for name $name")
            return
        }

//        event.runAllActions(data)

        val ensureAllDispatchEventsAreChecked: Unit = when (event) {
            Ready -> Ready.withJson(data) {
                bot.user = user
                bot.sessionId = sessionId

                Ready.actions.forEach { it() }
            }
            Resumed -> Resumed.withJson(data) {
                TODO("implement action on resume")
            }
            InvalidSession -> InvalidSession.withJson(data) {
                TODO("probably reconnect if you can on invalid session")
            }
            ChannelCreate -> ChannelCreate.withJson(data) {
                bot.channels += this
            }
            ChannelUpdate -> ChannelUpdate.withJson(data) {
                bot.channels += this
            }
            ChannelDelete -> ChannelDelete.withJson(data) {
                bot.channels -= this
            }
            ChannelPinsUpdate -> ChannelPinsUpdate.withJson(data) {
                TODO("do something (probably) on channel pin update")
            }
            GuildCreate -> GuildCreate.withJson(data) {
                bot.guilds += this
                GuildCreate.actions.forEach { it() }
            }
            MessageCreate -> MessageCreate.withJson(data) {
                bot.messages += this
                MessageCreate.actions.forEach { it() }
            }
            MessageUpdate -> MessageUpdate.withJson(data) {
                bot.messages += this
                MessageUpdate.actions.forEach { it() }
            }
        }
        /*
         OldDispatchEvent.GuildCreate -> {
             val guild: Guild = data.fromJson()
             for (action in guildCreateEvents) action(guild)
             bot.guilds += guild
         }
         OldDispatchEvent.GuildUpdate -> {
             val guild: Guild = data.fromJson()
             bot.guilds += guild
         }
         OldDispatchEvent.GuildDelete -> {
             val guild: Guild = data.fromJson()
             bot.guilds -= guild
         }
         OldDispatchEvent.GuildBanAdd -> {
             val banChangeInfo: GuildBanUpdateEvent = data.fromJson()
             // do something presumably
         }
         OldDispatchEvent.GuildBanRemove -> {
             val banChangeInfo: GuildBanUpdateEvent = data.fromJson()
             // do something presumably
         }
         OldDispatchEvent.GuildEmojisUpdate -> {
             val emojisEvent: GuildEmojisEvent = data.fromJson()
             // do something presumably
         }
         OldDispatchEvent.MessageCreate -> {
             val newMessage: Message = data.fromJson()
             for (action in messageCreateEvents) action(newMessage)
             bot.messages += newMessage
         }
         OldDispatchEvent.MessageUpdate -> {
             val newMessage: Message = data.fromJson()
             bot.messages += newMessage
         }
         OldDispatchEvent.MessageDelete -> {
             val deleteInfo: MessageDeleteEvent = data.fromJson()
             bot.messages -= deleteInfo.id
         }
         OldDispatchEvent.MessageDeleteBulk -> {
             val deleteInfo: MessageDeleteBulkEvent = data.fromJson()
             for (id in deleteInfo.ids) {
                 bot.messages -= id
             }
         }
         OldDispatchEvent.MessageReactionAdd -> {
             val reactionUpdate: MessageReactionUpdateEvent = data.fromJson()
         }
         OldDispatchEvent.MessageReactionRemove -> {
             val reactionUpdate: MessageReactionUpdateEvent = data.fromJson()
         }
         OldDispatchEvent.MessageReactionRemoveAll -> {
             val reactionUpdate: MessageReactionRemoveAllEvent = data.fromJson()
         }
         OldDispatchEvent.TypingStart -> {

         }
         OldDispatchEvent.UserUpdate -> {
             val user: User = data.fromJson()
//                users[getUser.id] = getUser
         }*/
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
        val message = GatewayPayload(payload.opcode.code, payload.toJsonTree(), sequenceNumber)
        sendWebsocket(message.toJson())
    }
}