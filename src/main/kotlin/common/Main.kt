package common

import common.commands.MessageCommand
import common.commands.ReactCommand
import common.util.now
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lib.dsl.Bot
import lib.dsl.on
import lib.dsl.send
import lib.dsl.updateStatus
import lib.model.Activity
import lib.model.ActivityType
import lib.model.Color.Companion.gold
import lib.model.channel.Channel
import lib.model.user.User
import lib.rest.http.httpRequests.getChannel
import lib.rest.http.httpRequests.getUser
import lib.rest.model.events.receiveEvents.*
import lib.rest.model.events.sendEvents.Status
import lib.util.fromJson
import java.io.File
import java.time.Instant

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
            updateStatus(Activity("Avalon - say !help", ActivityType.Game), Status.Online)
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

        on(GuildCreate) {
            println("[${now()}] Guild ${this.name} created! (${this.id})")
        }

        launch {
            // todo maybe detect if the bot is actually connected somehow
            while (true) {
                // todo file handler to auto put us in that dir
                File("src/main/resources/uptime").bufferedWriter().use {
                    it.write(Instant.now().toString())
                }
                println("[${now()}] Updated uptime file")
                val mins = 30L
                delay(1000 * 60 * mins)
            }
        }
    }
}