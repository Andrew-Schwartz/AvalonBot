package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.model.Channel
import lib.model.Guild
import lib.model.Message
import lib.model.User
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.createDM
import lib.rest.http.httpRequests.createMessage
import lib.rest.http.httpRequests.getChannel
import lib.rest.websocket.DiscordWebsocket
import lib.util.L
import lib.util.Store
import lib.util.pingNick

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Bot internal constructor(val token: String) {
    val authHeaders = mapOf("Authorization" to "Bot $token")

    lateinit var user: User

    var sessionId: String? = null

    val guilds: Store<Guild> = Store()
    val channels: Store<Channel> = Store()
    val messages: Store<Message> = Store()
    val users: Store<User> = Store()


    suspend fun Message.reply(content: String = "",
                              embed: RichEmbed = RichEmbed(),
                              ping: Boolean = false,
                              builder: suspend RichEmbed.() -> Unit = {}) {
        channel.send(
                content = content,
                embed = embed,
                pingTargets = if (ping) L[author] else emptyList(),
                builder = builder
        )
    }

    val Message.channel: Channel
        get() = runBlocking { getChannel(channelId) }

    suspend fun User.sendDM(content: String = "",
                            embed: RichEmbed = RichEmbed(),
                            builder: suspend RichEmbed.() -> Unit = {}) {
        createDM(id).send(
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
                             builder: suspend RichEmbed.() -> Unit = {}) {
        val text = pingTargets.joinToString(separator = "\n", postfix = content) { it.pingNick() }
        val embed = embed.apply { builder() }.takeIf { it != RichEmbed.empty }?.build()

        createMessage(this, CreateMessage(
                content = text,
                embed = embed,
                file = embed?.files
        ))
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun launchSocket() {
        DiscordWebsocket(this).run()
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun bot(token: String, λ: suspend Bot.() -> Unit): Unit = Bot(token).apply { λ() }.launchSocket()