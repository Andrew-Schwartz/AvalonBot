package lib.rest.websocket

import common.util.now
import common.util.onNull
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import lib.dsl.Bot
import lib.exceptions.InvalidSessionException
import lib.model.Activity
import lib.model.ActivityType
import lib.rest.client
import lib.rest.http.httpRequests.gateway
import lib.rest.model.GatewayOpcode
import lib.rest.model.GatewayOpcode.Dispatch
import lib.rest.model.GatewayPayload
import lib.rest.model.events.receiveEvents.*
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Add
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Remove
import lib.rest.model.events.receiveEvents.PresenceUpdate
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
    private var sessionId: String? = null
    private var authed = false

    private var lastAck: Instant? = null
    private var lastHeartbeat: Instant? = null
    private var strikes: Int = 0

    suspend fun run(): Nothing {
        defaultListeners()
        while (true) {
            println("[${now()}] Starting websocket")
            runCatching {
                client.wss(host = bot.gateway(), port = 443) {
                    // set up 'callback's to interact with ws from other functions
                    sendWebsocket = { send(it) }
                    close = { code, message ->
                        lastAck = null
                        lastHeartbeat = null
                        strikes = 0
                        close(CloseReason(code, message))
                    }

                    // Resume on reconnect
                    if (sessionId != null) {
                        val resume = Resume(bot.token, sessionId!!, sequenceNumber!!)
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
                                .onFailure { it.printStackTrace() }
                    }
                    close(CloseReason.Codes.GOING_AWAY, "Incoming is closed")
                }
            }.onFailure { println("[${now()}] caught $it") }
        }
    }

    private suspend fun receive(payload: GatewayPayload) {
        when (payload.opcode) {
            GatewayOpcode.Hello -> {
                initializeConnection(payload)
            }
            Dispatch -> {
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
            }
            GatewayOpcode.InvalidSession -> {
                println("[${now()}] recv Invalid Session: $payload")
                val resumable = payload.eventData!!.asBoolean
                if (!resumable) {
                    authed = false
                    sequenceNumber = null
                    sessionId = null
                    throw InvalidSessionException("Non Resumable session")
                }
            }
            else -> {
                println("[${now()}] should not receive ${payload.opcode}, it's content was ${payload.eventData}")
            }
        }
    }

    private suspend fun processDispatch(gatewayPayload: GatewayPayload) {
        sequenceNumber = gatewayPayload.sequenceNumber

        // Not null since these are guaranteed to be in dispatches
        val payload = gatewayPayload.eventData!!
        val name = gatewayPayload.eventName!!.replace("_", "")

        val kClass = DispatchEvent::class.sealedSubclasses
                .firstOrNull { it.simpleName?.toUpperCase() == name }
                .onNull { println("No DispatchEvent for $name, data is:\n$payload") }
                ?: return

        val event: DispatchEvent<*> = kClass.objectInstance!!

//        val payloadType = kClass
//                .supertypes
//                .first()
//                .arguments
//                .first()
//                .type!!

//        gson.fromJson(payload, payloadType.javaType)

        when (event) {
            Ready -> Ready.withJson(payload) {
                Ready.actions.forEach { it() }
            }
            Resumed -> Resumed.withJson(payload) {
                Resumed.actions.forEach { it() }
            }
            ChannelCreate -> ChannelCreate.withJson(payload) {
                bot.channels.add(this).run {
                    ChannelCreate.actions.forEach { it() }
                }
            }
            ChannelUpdate -> ChannelUpdate.withJson(payload) {
                bot.channels.add(this).run {
                    ChannelUpdate.actions.forEach { it() }
                }
            }
            ChannelDelete -> ChannelDelete.withJson(payload) {
                ChannelDelete.actions.forEach { it() }
//                bot.channels -= this
            }
            ChannelPinsUpdate -> ChannelPinsUpdate.withJson(payload) {
                ChannelPinsUpdate.actions.forEach { it() }
            }
            GuildCreate -> GuildCreate.withJson(payload) {
                bot.guilds.add(this).run {
                    GuildCreate.actions.forEach { it() }
                }
            }
            GuildUpdate -> GuildUpdate.withJson(payload) {
                bot.guilds.add(this).run {
                    GuildUpdate.actions.forEach { it() }
                }
            }
            GuildDelete -> GuildDelete.withJson(payload) {
                GuildDelete.actions.forEach { it() }
            }
            GuildBanAdd -> GuildBanAdd.withJson(payload) {
                GuildBanAdd.actions.forEach { it() }
            }
            GuildBanRemove -> GuildBanRemove.withJson(payload) {
                GuildBanRemove.actions.forEach { it() }
            }
            GuildEmojisUpdate -> GuildEmojisUpdate.withJson(payload) {
                GuildEmojisUpdate.actions.forEach { it() }
            }
            GuildIntegrationsUpdate -> GuildIntegrationsUpdate.withJson(payload) {
                GuildIntegrationsUpdate.actions.forEach { it() }
            }
            GuildMemberAdd -> GuildMemberAdd.withJson(payload) {
                GuildMemberAdd.actions.forEach { it() }
            }
            GuildMemberRemove -> GuildMemberRemove.withJson(payload) {
                GuildMemberRemove.actions.forEach { it() }
            }
            GuildMemberUpdate -> GuildMemberUpdate.withJson(payload) {
                GuildMemberUpdate.actions.forEach { it() }
            }
            GuildMembersChunk -> GuildMembersChunk.withJson(payload) {
                GuildMembersChunk.actions.forEach { it() }
            }
            GuildRoleCreate -> GuildRoleCreate.withJson(payload) {
                GuildRoleCreate.actions.forEach { it() }
            }
            GuildRoleUpdate -> GuildRoleUpdate.withJson(payload) {
                GuildRoleUpdate.actions.forEach { it() }
            }
            GuildRoleDelete -> GuildRoleDelete.withJson(payload) {
                GuildRoleDelete.actions.forEach { it() }
            }
            InviteCreate -> InviteCreate.withJson(payload) {
                InviteCreate.actions.forEach { it() }
            }
            InviteDelete -> InviteDelete.withJson(payload) {
                InviteDelete.actions.forEach { it() }
            }
            MessageCreate -> MessageCreate.withJson(payload) {
                bot.messages.add(this).run {
                    MessageCreate.actions.forEach { it() }
                }
            }
            MessageUpdate -> MessageUpdate.withJson(payload) {
                bot.messages.add(this).run {
                    MessageUpdate.actions.forEach { it() }
                }
            }
            MessageDelete -> MessageDelete.withJson(payload) {
                MessageDelete.actions.forEach { it() }
            }
            MessageDeleteBulk -> MessageDeleteBulk.withJson(payload) {
                MessageDeleteBulk.actions.forEach { it() }
            }
            MessageReactionAdd -> MessageReactionAdd.withJson(payload) {
                MessageReactionAdd.actions.forEach { it() }
                MessageReactionUpdate.actions.forEach {
                    with(MessageReactionUpdatePayload(userId, messageId, channelId, guildId, emoji, Add)) {
                        it()
                    }
                }
            }
            MessageReactionRemove -> MessageReactionRemove.withJson(payload) {
                MessageReactionRemove.actions.forEach { it() }
                MessageReactionUpdate.actions.forEach {
                    with(MessageReactionUpdatePayload(userId, messageId, channelId, guildId, emoji, Remove)) {
                        it()
                    }
                }
            }
            MessageReactionRemoveAll -> MessageReactionRemoveAll.withJson(payload) {
                MessageReactionRemoveAll.actions.forEach { it() }
            }
            MessageReactionRemoveEmoji -> MessageReactionRemoveEmoji.withJson(payload) {
                MessageReactionRemoveEmoji.actions.forEach { it() }
            }
            PresenceUpdate -> PresenceUpdate.withJson(payload) {
                PresenceUpdate.actions.forEach { it() }
            }
            TypingStart -> TypingStart.withJson(payload) {
                TypingStart.actions.forEach { it() }
            }
            UserUpdate -> UserUpdate.withJson(payload) {
                UserUpdate.actions.forEach { it() }
            }
            VoiceStateUpdate -> VoiceStateUpdate.withJson(payload) {
                VoiceStateUpdate.actions.forEach { it() }
            }
            VoiceServerUpdate -> VoiceServerUpdate.withJson(payload) {
                VoiceServerUpdate.actions.forEach { it() }
            }
            WebhookUpdate -> WebhookUpdate.withJson(payload) {
                WebhookUpdate.actions.forEach { it() }
            }
            MessageReactionUpdate -> throw IllegalStateException("Message Reaction Update isn't real")
        }
    }

    private suspend fun initializeConnection(payload: GatewayPayload) {
        if (!authed) {
            val identify = Identify(
                    bot.token,
                    ConnectionProperties(),
                    presence = GatewayStatus(
                            null,
                            Activity("Playin Avalon", ActivityType.Custom),
                            Status.Online,
                            false
                    ),
                    intents = DispatchEvent.intents.bits
            )
            sendGatewayEvent(identify)
            authed = true
        }
        val delayTime = payload.eventData!!.asJsonObject["heartbeat_interval"].asLong

        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                sequenceNumber?.let {
                    if (lastHeartbeat != null && lastAck != null && lastHeartbeat!!.isAfter(lastAck!!)) {
                        strikes++
                        println("[${now()}] ACK Strike $strikes")
                        if (strikes >= 3)
                            close(CloseReason.Codes.SERVICE_RESTART, "ACK not recent enough, closing websocket")
                    }
                    sendGatewayEvent(Heartbeat(it))
                    lastHeartbeat = Instant.now()
                    delay(delayTime)
                }
            }
        }
    }

    private suspend fun sendGatewayEvent(payload: SendEvent) {
        val message = GatewayPayload(payload.opcode.code, payload.toJsonTree())
        sendWebsocket(message.toJson())
    }

    private fun defaultListeners() {
        Ready.actions.add(0) {
            bot.user = this.user
            this@DiscordWebsocket.sessionId = sessionId
        }
    }
}