package main

import avalon.Colors.neutral
import avalon.characters.Character
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.bot
import lib.dsl.command
import lib.dsl.on
import lib.model.channel.Channel
import lib.model.user.User
import lib.rest.http.httpRequests.getChannel
import lib.rest.http.httpRequests.getUser
import lib.rest.model.events.receiveEvents.*
import lib.util.fromJson
import main.commands.Command
import main.util.A
import main.util.formatIterable
import java.io.File

//val config: ConfigJson = File("src/main/resources/config/config.json").readText().fromJson()

val players: MutableMap<String, User> = mutableMapOf()
val roles: ArrayList<Character> = ArrayList()

val avalonLogo: File = File("src/main/resources/images/avalon/avalonLogo.png")
val leaderCrown: File = File("src/main/resources/images/avalon/leaderCrown.jpg")

lateinit var steadfast: User
lateinit var kts: Channel

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId, ktsId) = javaClass.getResourceAsStream("/config/config.json")
            .bufferedReader()
            .readText()
            .fromJson<ConfigJson>()

    bot(token) {
        //        blockUntil { false }

        steadfast = getUser(sfId)
        kts = getChannel(ktsId)

        on(Ready) {
            //            steadfast.sendDM {
            kts.send {
                title = "${this@bot.user.username} is logged on!!"
                color = neutral
                timestamp()
                url = "https://github.com/Andrew-Schwartz/AvalonBot"
            }
        }

//        on(GuildCreate) {
//            if (id == "353205950699667456".snowflake()) {
//                channels?.forEach {
//                    val lastMessage = it.lastMessage
//                    val lastMessages = getMessages(GetChannelMessages.before(it.id, lastMessage.id))
//                    var message: Message? = null
//                    do {
//                        message = lastMessages.firstOrNull { "Ben do you want to go apple picking" in it.content }
//                    } while (message == null)
//                    println(message)
//                }
//            }
//        }

        on(MessageDelete) {
            message(bot = this@bot).run {
                steadfast.sendDM {
                    //                    title = "Message from ${this@run.author.username} in ${this@run.channel.nameOrUser} deleted!"
                    title = "Message from ${this@run.author.username} in " +
                            when (this@run.guild) {
                                null -> "a DM with ${this@run.channel.recipients?.formatIterable { it.username }} "
                                else -> "${this@run.guild!!.name}/${this@run.channel.name} "
                            } + "was deleted"
                    if (content.isNotEmpty()) {
                        description = content
                    }
                    for (attachment in attachments) {
                        addField(attachment.filename, attachment.proxyUrl)
                    }
                    for (embed in embeds) {
                        addField("Embedded:", embed.toString())
                    }
                }
            }
        }

        on(MessageUpdate) {
            if (author.isBot == true) return@on
            steadfast.sendDM {
                //                title = "Message from ${this@on.author.username} in ${this@on.channel.nameOrUser} edited!"
                title = "Message from ${this@on.author.username} in " +
                        when (this@on.guild) {
                            null -> "a DM with ${this@on.channel.recipients?.formatIterable { it.username }} "
                            else -> "${this@on.guild!!.name}/${this@on.channel.name} "
                        } + "was edited!"
                if (mostRecent?.content?.isEmpty() == false) {
                    description = mostRecent?.content
                }
                for (attachment in mostRecent?.attachments ?: emptyArray()) {
                    addField(attachment.filename, attachment.proxyUrl)
                }
                for (embed in mostRecent?.embeds ?: emptyArray()) {
                    addField("Embedded:", embed.toString())
                }
            }
        }

        on(PresencesReplace) {
            println("presence replace array: ${this.contentDeepToString()}")
            if (this.isNotEmpty()) {
                steadfast.sendDM {
                    title = "PresencesReplace"
                    description = this@on.contentDeepToString()
                    timestamp()
                }
            }
        }

        command(prefix, events = *A[MessageCreate, MessageUpdate]) {
            Command.run(this@bot, this, prefix)
        }
    }
}