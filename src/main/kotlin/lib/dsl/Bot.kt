package lib.dsl

import common.util.M
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.websocket.DiscordWebsocket
import lib.util.Store
import java.time.OffsetDateTime

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
object Bot {
    lateinit var headers: Map<String, String>; private set
    var websocket: DiscordWebsocket? = null; private set
    var firstLogInTime: OffsetDateTime? = null; internal set
    var logInTime: OffsetDateTime? = null; internal set

    val pinnedMessages: MutableList<Message> = mutableListOf()

    lateinit var user: User; internal set

    val guilds: Store<Guild> = Store()
    val channels: Store<Channel> = Store()
    val messages: Store<Message> = Store()
    val users: Store<User> = Store()

    suspend fun launchSocket(token: String) {
        websocket = DiscordWebsocket(token)
        websocket!!.run()
    }

    suspend operator fun invoke(token: String, action: suspend Bot.() -> Unit) {
        if (websocket != null) throw IllegalStateException("The bot can only be started once")
        headers = M["Authorization" to "Bot $token"]
        Bot.apply { action() }.launchSocket(token)
    }
}