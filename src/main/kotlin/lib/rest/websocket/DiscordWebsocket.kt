package lib.rest.websocket

import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import lib.dsl.Bot
import lib.dsl.eventListeners
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
import lib.util.toJsonElement
import kotlin.system.exitProcess

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class DiscordWebsocket(val bot: Bot) {
    lateinit var sendWebsocket: suspend (String) -> Unit

    private var heartbeatJob: Job? = null
    private var sequenceNumber: Int? = null

    suspend fun run(): Nothing {
        client.wss(host = bot.gateway(), port = 443) {
            sendWebsocket = { send(it) }

            launch {
                println("closed because ${this@wss.closeReason.await()}")
            }

            eventLoop@ while (!incoming.isClosedForReceive) {
                val message: Frame.Text? = incoming.poll() as Frame.Text?

                // run all eventListeners whose predicate is matched, then remove them
                eventListeners -= eventListeners.filter { it.first() }.onEach { it.second() }

                message?.let {
                    try {
                        val text = it.readText()
//                        println(text)
                        receive(text.fromJson())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
//            var i = 0
//            while (!incoming.isClosedForReceive) {
//                try {
//                    val message = incoming.receive() //incoming.receiveOrNull()
//                    incoming.poll()
//
//                    eventListeners -= eventListeners.filter { it.first() }.onEach { println("got one!"); it.second() }
//
////                    for (it in eventListeners) {
////                        if (it.first()) {
////                            it.second()
////                            eventListeners.remove(it)
////                            continue
////                        }
////                    }
//
//                    receive((message as Frame.Text).readText().fromJson())
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    println("WSS getChannel closed: ${e.message}")
////                    exitProcess(1)
//                }
//                println("yo #${i++}")
//            }
            println("done with while")
        }

        println("closed")
        client.close()
        exitProcess(1)
    }

    private suspend fun receive(payload: GatewayPayload): Unit = when (payload.opcode) {
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
            println("should not receive ${payload.opcode}, it's content was ${payload.eventData}")
//            throw IllegalStateException("${payload.opcode} should not be received")
        }
    }

    private suspend fun processDispatch(payload: GatewayPayload) {
        sequenceNumber = payload.sequenceNumber

        // Not null since these are guaranteed to be in dispatches
        val data = payload.eventData!!
        val name = payload.eventName!!.replace("_", "")

        val event: DispatchEvent<*> = try {
            DispatchEvent::class.sealedSubclasses.first {
                val className = it.simpleName!!.toUpperCase() // null if anonymous, no subclasses are anonymous
                className == name
            }.objectInstance!! // all dispatch events are objects
        } catch (e: NoSuchElementException) {
            println("no DispatchEvent for name $name, data is:\n$data")
            return
        }

//        event.runAllActions(data)

        when (event) {
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
                ChannelCreate.actions.forEach { it() }
            }
            ChannelUpdate -> ChannelUpdate.withJson(data) {
                bot.channels += this
                ChannelUpdate.actions.forEach { it() }
            }
            ChannelDelete -> ChannelDelete.withJson(data) {
                bot.channels -= this
                ChannelDelete.actions.forEach { it() }
            }
            ChannelPinsUpdate -> ChannelPinsUpdate.withJson(data) {
                ChannelPinsUpdate.actions.forEach { it() }
            }
            GuildCreate -> GuildCreate.withJson(data) {
                bot.guilds += this
                GuildCreate.actions.forEach { it() }
            }
            GuildUpdate -> GuildUpdate.withJson(data) {
                GuildUpdate.actions.forEach { it() }
            }
            GuildDelete -> GuildDelete.withJson(data) {
                GuildDelete.actions.forEach { it() }
            }
            GuildBanAdd -> GuildBanAdd.withJson(data) {
                GuildBanAdd.actions.forEach { it() }
            }
            GuildBanRemove -> GuildBanRemove.withJson(data) {
                GuildBanRemove.actions.forEach { it() }
            }
            GuildEmojisUpdate -> GuildEmojisUpdate.withJson(data) {
                GuildEmojisUpdate.actions.forEach { it() }
            }
            GuildIntegrationsUpdate -> GuildIntegrationsUpdate.withJson(data) {
                GuildIntegrationsUpdate.actions.forEach { it() }
            }
            GuildMemberAdd -> GuildMemberAdd.withJson(data) {
                GuildMemberAdd.actions.forEach { it() }
            }
            GuildMemberRemove -> GuildMemberRemove.withJson(data) {
                GuildMemberRemove.actions.forEach { it() }
            }
            GuildMemberUpdate -> GuildMemberUpdate.withJson(data) {
                GuildMemberUpdate.actions.forEach { it() }
            }
            GuildMembersChunk -> GuildMembersChunk.withJson(data) {
                GuildMembersChunk.actions.forEach { it() }
            }
            GuildRoleCreate -> GuildRoleCreate.withJson(data) {
                GuildRoleCreate.actions.forEach { it() }
            }
            GuildRoleUpdate -> GuildRoleUpdate.withJson(data) {
                GuildRoleUpdate.actions.forEach { it() }
            }
            GuildRoleDelete -> GuildRoleDelete.withJson(data) {
                GuildRoleDelete.actions.forEach { it() }
            }
            MessageCreate -> MessageCreate.withJson(data) {
                bot.messages += this
                MessageCreate.actions.forEach { it() }
            }
            MessageUpdate -> MessageUpdate.withJson(data) {
                bot.messages += this
                MessageUpdate.actions.forEach { it() }
            }
            MessageDelete -> MessageDelete.withJson(data) {
                MessageDelete.actions.forEach { it() }
//                bot.messages -= getC
            }
            MessageDeleteBulk -> MessageDeleteBulk.withJson(data) {
                MessageDeleteBulk.actions.forEach { it() }
            }
            MessageReactionAdd -> MessageReactionAdd.withJson(data) {
                MessageReactionAdd.actions.forEach { it() }
            }
            MessageReactionRemove -> MessageReactionRemove.withJson(data) {
                MessageReactionRemove.actions.forEach { it() }
            }
            MessageReactionRemoveAll -> MessageReactionRemoveAll.withJson(data) {
                MessageReactionRemoveAll.actions.forEach { it() }
            }
            PresenceUpdate -> PresenceUpdate.withJson(data) {
                PresenceUpdate.actions.forEach { it() }
            }
            PresencesReplace -> PresencesReplace.withJson(data) {
                PresencesReplace.actions.forEach { it() }
            }
            TypingStart -> TypingStart.withJson(data) {
                TypingStart.actions.forEach { it() }
            }
            UserUpdate -> UserUpdate.withJson(data) {
                UserUpdate.actions.forEach { it() }
            }
            VoiceStateUpdate -> VoiceStateUpdate.withJson(data) {
                VoiceStateUpdate.actions.forEach { it() }
            }
            VoiceServerUpdate -> VoiceServerUpdate.withJson(data) {
                VoiceServerUpdate.actions.forEach { it() }
            }
            WebhookUpdate -> WebhookUpdate.withJson(data) {
                WebhookUpdate.actions.forEach { it() }
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
        val message = GatewayPayload(payload.opcode.code, payload.toJsonElement(), sequenceNumber)
        sendWebsocket(message.toJson())
    }
}