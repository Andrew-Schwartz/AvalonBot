package common

import common.commands.MessageCommand
import common.commands.ReactCommand
import common.util.now
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.Bot
import lib.dsl.on
import lib.dsl.send
import lib.model.Color.Companion.gold
import lib.model.channel.Channel
import lib.model.user.User
import lib.rest.http.httpRequests.getChannel
import lib.rest.http.httpRequests.getUser
import lib.rest.model.events.receiveEvents.*
import lib.util.fromJson

lateinit var steadfast: User
lateinit var kts: Channel

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId, ktsId) = javaClass.getResourceAsStream("/config/config.json")
            .bufferedReader()
            .readText()
            .fromJson<ConfigJson>()

    Bot(token) {
        steadfast = getUser(sfId)
        kts = getChannel(ktsId)

        on(Ready) {
            println("[${now()}] Ready!")
            kts.send {
                title = this@Bot.user.username + " is logged on!!"
                color = gold
                timestamp()
                url = "https://github.com/Andrew-Schwartz/AvalonBot"
            }
        }

        on(Resumed) {
            println("[${now()}] resumed: $this")
            kts.send {
                title = this@Bot.user.username + " has resumed!"
                color = gold
                timestamp()
            }
        }

        on(MessageCreate, MessageUpdate) {
            if (author.isBot != true && content.startsWith(prefix)) {
                MessageCommand.run(this, prefix)
            }
        }

        on(MessageReactionUpdate) {
            if (user().isBot != true) {
                ReactCommand.run(this)
            }
        }
    }
}