package common

import common.commands.MessageCommand
import common.commands.ReactCommand
import common.util.now
import common.util.onNull
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.Bot
import lib.dsl.bot
import lib.dsl.on
import lib.model.ChannelId
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

            val avChannel = getChannel(ChannelId("720860398282080267"))
            val lastMessage = avChannel.lastMessage()
                    ?.onNull { println("no last message") } ?: return@on

//            var unpins = 0
//            while (unpins < 4) {
//                val messages = getMessages(GetChannelMessages.before(avChannel, lastMessage))
//                for (message in messages) {
//                    if (message.pinned) {
//                        message.unpin()
//                        unpins += 1
//                    }
//                }
//            }
        }

        on(Resumed) {
            println("[${now()}] resumed: $this")
            kts.send {
                title = this@bot.user.username + " has resumed!"
                color = gold
                timestamp()
            }
        }

        on(MessageCreate, MessageUpdate) {
            if (content.startsWith(prefix) && author.isBot != true) {
                MessageCommand.run(this, prefix)
            }
        }

        on(MessageReactionUpdate) {
            ReactCommand.run(this)
        }

        // Adds necessary intent
        MessageReactionAdd
    }
}