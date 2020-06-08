package lib.dsl

import common.util.A
import common.util.M
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.exceptions.PermissionException
import lib.model.Snowflake
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.*
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import lib.rest.websocket.DiscordWebsocket
import lib.util.Store
import lib.util.ping
import java.time.OffsetDateTime

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Bot internal constructor(val token: String) {
    val authHeaders = M["Authorization" to "Bot $token"]
    val websocket = DiscordWebsocket(this)
    var logInTime: OffsetDateTime? = null

    val pinnedMessages: MutableList<Message> = mutableListOf()

    lateinit var user: User

    val guilds: Store<Guild> = Store()
    val channels: Store<Channel> = Store()
    val messages: Store<Message> = Store()
    val users: Store<User> = Store()

    suspend fun Message.reply(
            content: String = "",
            embed: RichEmbed = RichEmbed(),
            ping: Boolean = false,
            builder: suspend RichEmbed.() -> Unit = {}
    ): Message = channel.send(
            content = content,
            embed = embed,
            pingTargets = if (ping) A[author] else emptyArray(),
            builder = builder
    )

    suspend fun Message.edit(
            content: String? = null,
            embed: RichEmbed? = null,
            pingTargets: Array<User> = emptyArray(),
            builder: (suspend RichEmbed.() -> Unit)? = null
    ): Message {
        if (author != user) throw PermissionException("Can only edit messages you have sent")

        val pingText = pingTargets.joinToString(separator = "\n") { it.ping() }

        val text = when {
            content == null && pingTargets.isEmpty() -> null
            content == null -> pingText
            else -> pingText + content
        }

        val embed = when {
            embed == null && builder == null -> null
            embed == null -> RichEmbed().apply { builder!!.invoke(RichEmbed()) }.build()
            else -> embed.apply { builder?.invoke(this) }.build()
        }

        return editMessage(channelId, id, text, embed)
    }

    suspend fun Message.react(emoji: Char) {
        createReaction(channel.id, id, emoji)
    }

    suspend fun Message.reactions(emoji: Char) = getReactions(channelId, id, emoji)

    suspend fun Message.reactions(vararg emojis: Char): List<Array<User>> = emojis.map { reactions(it) }

    suspend fun User.getDM(): Channel = createDM(id)

    suspend fun User.sendDM(
            content: String = "",
            embed: RichEmbed = RichEmbed(),
            builder: suspend RichEmbed.() -> Unit = {}
    ): Message {
        return getDM().send(
                content = content,
                embed = embed,
                builder = builder
        )
    }

    suspend fun Channel.send(
            content: String = "",
            embed: RichEmbed = RichEmbed(),
            pingTargets: Array<User> = emptyArray(),
            builder: suspend RichEmbed.() -> Unit = {}
    ): Message {
        val text = pingTargets.joinToString(separator = "\n", postfix = content) { it.ping() }

        @Suppress("NAME_SHADOWING")
        val embed = embed.apply { builder() }.takeIf { it.isNotEmpty }?.build()

        return createMessage(this, CreateMessage(
                content = text,
                embed = embed,
                file = embed?.files
        ))
    }

    suspend fun Channel.startTyping() {
        triggerTypingIndicator(id)
    }

    suspend fun deleteMessages(vararg messages: Message) {
        when (messages.size) {
            0 -> return
            1 -> {
                val message = messages[0]
                deleteMessage(message.channelId, messages[0].id)
            }
            else -> messages.asSequence()
                    .groupBy { it.channelId }
                    .forEach { (channelId, group) ->
                        group.chunked(100).forEach {
                            bulkDeleteMessages(channelId, it.toSet())
                        }
                    }
        }
    }

    suspend fun launchSocket() {
        websocket.run()
    }

    val Snowflake.user: User
        get() = runBlocking { getUser(this@user) }

    val Message.channel: Channel
        get() = runBlocking { getChannel(channelId) }

    val Message.guild: Guild?
        get() = runBlocking {
            guildId ?: return@runBlocking null
            getGuild(guildId)
        }

    suspend fun Message.pin() {
        pinnedMessages += this
        addPin(channelId, id)
    }

    val Channel.lastMessage: Message
        get() = runBlocking { getMessage(id, lastMessageId ?: throw IllegalStateException("Last message was null")) }

    val Channel.guild: Guild?
        get() = runBlocking { getGuild(guildId ?: return@runBlocking null) }

    val MessageReactionUpdatePayload.user: User
        get() = runBlocking { getUser(userId) }
}

@Suppress("NonAsciiCharacters")
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun bot(token: String, λ: suspend Bot.() -> Unit): Unit = Bot(token).apply { λ() }.launchSocket()