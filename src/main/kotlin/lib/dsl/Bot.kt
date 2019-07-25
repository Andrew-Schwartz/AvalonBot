package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.model.Channel
import lib.model.Guild
import lib.model.Message
import lib.model.User
import lib.rest.RateLimitInfo
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.*
import lib.rest.websocket.DiscordWebsocket
import lib.util.L
import lib.util.Store
import lib.util.ping

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Bot internal constructor(val token: String) {
    val authHeaders = mapOf("Authorization" to "Bot $token")
    private val socket = DiscordWebsocket(this)
    val rateLimitInfo: RateLimitInfo = RateLimitInfo(null, null, null, null)

    val pinnedMessages: MutableList<Message> = mutableListOf()

    lateinit var user: User

    var sessionId: String? = null

    val guilds: Store<Guild> = Store()
    val channels: Store<Channel> = Store()
    val messages: Store<Message> = Store()
    val users: Store<User> = Store()

    suspend fun Message.reply(content: String = "",
                              embed: RichEmbed = RichEmbed(),
                              ping: Boolean = false,
                              builder: suspend RichEmbed.() -> Unit = {}
    ): Message = channel.send(
            content = content,
            embed = embed,
            pingTargets = if (ping) L[author] else emptyList(),
            builder = builder
    )


    suspend fun Message.react(emoji: Char) {
        createReaction(channel.id, id, emoji)
    }

    suspend fun Message.reactions(emoji: Char) = getReactions(channelId, id, emoji)
    suspend fun Message.reactions(vararg emojis: Char): List<Array<User>> = emojis.map { reactions(it) }

    suspend fun User.getDM(): Channel = createDM(id)


    suspend fun User.sendDM(content: String = "",
                            embed: RichEmbed = RichEmbed(),
                            builder: suspend RichEmbed.() -> Unit = {}
    ): Message {
        return getDM().send(
                content = content,
                embed = embed,
                builder = builder
        )
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun Channel.send(content: String = "",
                             embed: RichEmbed = RichEmbed(),
                             pingTargets: List<User> = emptyList(),
                             builder: suspend RichEmbed.() -> Unit = {}
    ): Message {
        val text = pingTargets.joinToString(separator = "\n", postfix = content) { it.ping() }
        @Suppress("NAME_SHADOWING")
        val embed = embed.apply { builder() }.takeIf { it != RichEmbed.empty }?.build()

        return createMessage(this, CreateMessage(
                content = text,
                embed = embed,
                file = embed?.files
        ))
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun launchSocket() {
        socket.run()
    }

    val Message.channel: Channel
        get() = runBlocking { getChannel(channelId) }

    suspend fun Message.pin() {
        pinnedMessages += this
        addPin(channelId, id)
    }

    val Channel.lastMessage: Message
        get() = runBlocking { getMessage(id, lastMessageId ?: throw IllegalStateException("Last message was null")) }

    val Channel.guild: Guild?
        get() = runBlocking { getGuild(guildId ?: return@runBlocking null) }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun bot(token: String, λ: suspend Bot.() -> Unit): Unit = Bot(token).apply { λ() }.launchSocket()