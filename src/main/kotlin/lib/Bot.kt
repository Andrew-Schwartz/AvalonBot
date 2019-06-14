package lib

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.*
import lib.rest.websocket.DiscordWebsocket

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class Bot internal constructor(val token: String) {
    val socket = DiscordWebsocket(this)

    lateinit var user: User

    var sessionId: String? = null

    val guilds: MutableMap<Snowflake, Guild> = hashMapOf()
    val channels: MutableMap<Snowflake, Channel> = hashMapOf()
    val messages: MutableMap<Snowflake, Message> = hashMapOf()
}
