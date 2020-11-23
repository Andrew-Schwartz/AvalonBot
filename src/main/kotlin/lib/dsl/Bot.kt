package lib.dsl

import common.util.M
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import lib.model.ChannelId
import lib.model.GuildId
import lib.model.MessageId
import lib.model.UserId
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.websocket.DiscordWebsocket
import lib.util.Store
import java.time.OffsetDateTime

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
object Bot : CoroutineScope by CoroutineScope(GlobalScope.coroutineContext) {
    lateinit var headers: Map<String, String>; private set
    var websocket: DiscordWebsocket? = null; private set
    var firstLogInTime: OffsetDateTime? = null; internal set
    var logInTime: OffsetDateTime? = null; internal set

    val pinnedMessages: MutableList<Message> = mutableListOf()

    lateinit var user: User; internal set

    val guilds: Store<GuildId, Guild> = Store()
    val channels: Store<ChannelId, Channel> = Store()
    val messages: Store<MessageId, Message> = Store()
    val users: Store<UserId, User> = Store()

    suspend operator fun invoke(token: String, action: suspend Bot.() -> Unit) {
        if (websocket != null) throw IllegalStateException("The bot can only be started once")
        headers = M["Authorization" to "Bot $token"]

        this.action()

        websocket = DiscordWebsocket(token)
        websocket!!.run()
    }
}