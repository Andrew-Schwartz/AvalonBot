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
import lib.util.Store
import lib.util.ping

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Bot internal constructor(val token: String) {
    val authHeaders = mapOf("Authorization" to "Bot $token")

    lateinit var user: User

    var sessionId: String? = null

    //    val guilds: MutableMap<Snowflake, Guild> = hashMapOf()
//    val channels: MutableMap<Snowflake, Channel> = hashMapOf()
//    val messages: MutableMap<Snowflake, Message> = hashMapOf()
//    val users: MutableMap<Snowflake, User> = hashMapOf()
//
    val guilds: Store<Guild> = Store()
    val channels: Store<Channel> = Store()
    val messages: Store<Message> = Store()
    val users: Store<User> = Store()

    suspend fun Message.reply(content: String) {
        getChannel(channelId).send(content = content)
    }

    suspend fun Message.reply(embed: RichEmbed = RichEmbed(),
                              ping: Boolean = false,
                              builder: suspend RichEmbed.() -> Unit) {
        getChannel(channelId).send(embed = embed, pingTargets = if (ping) listOf(author) else emptyList(), builder = builder)
    }

    val Message.channel: Channel
        get() = runBlocking { getChannel(channelId) }

    suspend fun User.sendDM(content: String) {
        createDM(id).send(content = content)
    }

    suspend fun User.sendDM(embed: RichEmbed = RichEmbed(), builder: suspend RichEmbed.() -> Unit) {
        createDM(id).send(embed = embed, builder = builder)
    }

//    suspend fun Channel.send(content: String) {
//        createMessage(this, CreateMessage(content = content))
//    }

    suspend fun Channel.send(content: String = "",
                             embed: RichEmbed = RichEmbed(),
                             pingTargets: List<User> = listOf(),
                             builder: suspend RichEmbed.() -> Unit = {}) {
        val text = pingTargets.joinToString(separator = "\n", postfix = content) { it.ping() }
        val embed = embed.apply { builder() }.takeIf { it != RichEmbed.empty }?.build()

        createMessage(this, CreateMessage(
                content = text,
                embed = embed,
                file = TODO(),
                payloadJson = ""
        ))
    }

//    suspend fun Channel.send(embed: RichEmbed = RichEmbed(),
//                             ping: User? = null,
//                             builder: suspend RichEmbed.() -> Unit) {
//        createMessage(this, CreateMessage(
//                content = ping?.let { "<@${it.id}>" } ?: "",
//                embed = embed.apply { builder() }.build())
//        )


    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun launchSocket() {
        DiscordWebsocket(this).run()
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun bot(token: String, λ: suspend Bot.() -> Unit) {
    val bot = Bot(token).apply { λ() }

    bot.launchSocket()
}