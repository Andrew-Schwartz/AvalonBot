package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.*
import lib.rest.http.CreateDM
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.createDM
import lib.rest.http.httpRequests.createMessage
import lib.rest.http.httpRequests.getChannel
import lib.rest.websocket.DiscordWebsocket

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class Bot internal constructor(val token: String) {
    lateinit var user: User

    var sessionId: String? = null

    val guilds: MutableMap<Snowflake, Guild> = hashMapOf()
    val channels: MutableMap<Snowflake, Channel> = hashMapOf()
    val messages: MutableMap<Snowflake, Message> = hashMapOf()

    suspend fun Message.reply(content: String) {
        getChannel(channelId.also(::println)).sendMessage(content)
    }

    suspend fun User.sendDM(content: String) {
        val channel = createDM(CreateDM(id.value))

        channel.sendMessage(content)
    }

    suspend fun Channel.sendMessage(content: String) {
        createMessage(this, CreateMessage(content = content))
    }

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