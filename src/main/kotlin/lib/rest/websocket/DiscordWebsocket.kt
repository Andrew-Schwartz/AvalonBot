package lib.rest.websocket

import common.util.onNull
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import lib.dsl.Bot
import lib.model.Activity
import lib.model.ActivityType
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

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class DiscordWebsocket(val bot: Bot) {
    private lateinit var sendWebsocket: suspend (String) -> Unit
    lateinit var close: suspend (CloseReason.Codes, message: String) -> Unit

    private var heartbeatJob: Job? = null
    private var sequenceNumber: Int? = null
    var sessionId: String? = null

    private var lastAck: Instant? = null
    private var lastHeartbeat: Instant? = null

    suspend fun run(): Nothing {
        while (true) {
            println("Starting websocket")
            client.wss(host = bot.gateway(), port = 443) {
                sendWebsocket = { send(it) }
                close = { code, message -> close(CloseReason(code, message)) }

                launch {
                    println("closed because ${closeReason.await()}")
                }

                if (sessionId != null) {
                    val resume = Resume(bot.token, sessionId!!, sequenceNumber!!)
                    println("Resuming: $resume")
                    sendGatewayEvent(resume)
                }

                eventLoop@ while (!incoming.isClosedForReceive) {
                    val message = incoming.receive() as Frame.Text

                    runCatching { receive(message.readText().fromJson()) }
                            .onFailure { it.printStackTrace() }
                }
                close(CloseReason(CloseReason.Codes.GOING_AWAY, "Incoming is closed"))
            }
        }

//        run()
//        client.close()
//        exitProcess(1)
    }

    private suspend fun receive(payload: GatewayPayload): Unit = when (payload.opcode) {
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
            println(payload)
            TODO("implement Reconnect")
        }
        GatewayOpcode.InvalidSession -> {
            println(payload)
            TODO("implement InvalidSession")
        }
        else -> {
            println("should not receive ${payload.opcode}, it's content was ${payload.eventData}")
        }
    }

    private suspend fun processDispatch(payload: GatewayPayload) {
        sequenceNumber = payload.sequenceNumber

        // Not null since these are guaranteed to be in dispatches
        val data = payload.eventData!!
        val name = payload.eventName!!.replace("_", "")

        val event: DispatchEvent<*> = DispatchEvent::class.sealedSubclasses
                .firstOrNull { it.simpleName?.toUpperCase() == name }
                ?.objectInstance
                .onNull { println("No DispatchEvent for $name, data is:\n$data") }
                ?: return

//        event.runAllActions(data)

        when (event) {
            Ready -> Ready.withJson(data) {
                bot.user = user
                this@DiscordWebsocket.sessionId = sessionId

                Ready.actions.forEach { it() }
            }
            Resumed -> Resumed.withJson(data) {
                println(this)
                TODO("implement action on resume")
            }
            InvalidSession -> InvalidSession.withJson(data) {
                TODO("probably reconnect if you can on invalid session")
            }
            ChannelCreate -> ChannelCreate.withJson(data) {
                bot.channels.add(this).run {
                    ChannelCreate.actions.forEach { it() }
                }
            }
            ChannelUpdate -> ChannelUpdate.withJson(data) {
                bot.channels.add(this).run {
                    ChannelUpdate.actions.forEach { it() }
                }
            }
            ChannelDelete -> ChannelDelete.withJson(data) {
                ChannelDelete.actions.forEach { it() }
//                bot.channels -= this
            }
            ChannelPinsUpdate -> ChannelPinsUpdate.withJson(data) {
                ChannelPinsUpdate.actions.forEach { it() }
            }
            GuildCreate -> GuildCreate.withJson(data) {
                bot.guilds.add(this).run {
                    GuildCreate.actions.forEach { it() }
                }
            }
            GuildUpdate -> GuildUpdate.withJson(data) {
                bot.guilds.add(this).run {
                    GuildUpdate.actions.forEach { it() }
                }
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
                bot.messages.add(this).run {
                    MessageCreate.actions.forEach { it() }
                }
            }
            MessageUpdate -> MessageUpdate.withJson(data) {
                bot.messages.add(this).run {
                    MessageUpdate.actions.forEach { it() }
                }
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
        val identify = Identify(
                bot.token,
                ConnectionProperties(),
                presence = GatewayStatus(
                        null,
                        Activity("Playin Avalon", ActivityType.Custom),
                        Status.Online,
                        false
                ))
        sendGatewayEvent(identify)
        val delayTime = payload.eventData!!.asJsonObject["heartbeat_interval"].asLong

        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                sequenceNumber?.let {
                    if (lastHeartbeat?.isAfter(lastAck ?: Instant.MIN) == true) {
                        println("ACK not recent enough, closing websocket")
                        close(CloseReason.Codes.SERVICE_RESTART, "No Heartbeat ACK, reconnecting")
                    }
                    sendGatewayEvent(Heartbeat(it))
                    lastHeartbeat = Instant.now()
                    delay(delayTime)
                }
            }
        }
    }

    private suspend fun sendGatewayEvent(payload: SendEvent) {
        val message = GatewayPayload(payload.opcode.code, payload.toJsonTree(), sequenceNumber)
        sendWebsocket(message.toJson())
    }
}