package common

import common.commands.Command
import common.util.A
import common.util.now
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.Bot
import lib.dsl.bot
import lib.dsl.command
import lib.dsl.on
import lib.model.Color.Companion.gold
import lib.model.channel.Channel
import lib.model.user.User
import lib.rest.http.httpRequests.getChannel
import lib.rest.http.httpRequests.getUser
import lib.rest.model.events.receiveEvents.*
import lib.util.fromJson

lateinit var steadfast: User
lateinit var kts: Channel

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
lateinit var bot: Bot

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId, ktsId) = javaClass.getResourceAsStream("/config/config.json")
            .bufferedReader()
            .readText()
            .fromJson<ConfigJson>()

    bot(token) {
        bot = this

        steadfast = getUser(sfId)
        kts = getChannel(ktsId)

        on(Ready) {
            println("[${now()}] Ready!")
            kts.send {
                title = this@bot.user.username + " is logged on!!"
                color = gold
                timestamp()
                url = "https://github.com/Andrew-Schwartz/AvalonBot"
            }
        }

        on(Resumed) {
            println("[${now()}] resumed: $this")
            kts.send {
                title = this@bot.user.username + " has resumed!"
                color = gold
                timestamp()
            }
        }

        command(prefix, events = *A[MessageCreate, MessageUpdate]) {
            Command.run(this, prefix)
        }


        // Adds necessary intent
        MessageReactionAdd
    }
}