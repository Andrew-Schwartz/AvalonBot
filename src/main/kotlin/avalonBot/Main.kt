package avalonBot

import avalonBot.Colors.neutral
import avalonBot.characters.Character
import avalonBot.commands.Command
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
import lib.util.A
import lib.util.formatIterable
import lib.util.fromJson
import java.io.File

//val config: ConfigJson = File("src/main/resources/config/config.json").readText().fromJson()

val players: MutableMap<String, User> = mutableMapOf()
val roles: ArrayList<Character> = ArrayList()

val avalonLogo: File = File("src/main/resources/images/avalonLogo.png")
val leaderCrown: File = File("src/main/resources/images/leaderCrown.jpg")

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