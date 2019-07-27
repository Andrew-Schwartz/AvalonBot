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
import lib.model.user.User
import lib.rest.http.httpRequests.createDM
import lib.rest.http.httpRequests.getUser
import lib.rest.model.events.receiveEvents.MessageDelete
import lib.rest.model.events.receiveEvents.Ready
import lib.util.fromJson
import java.io.File

val config: ConfigJson = File("src/main/resources/config.json").readText().fromJson()

val players: MutableMap<String, User> = mutableMapOf()
val roles: ArrayList<Character> = ArrayList()

val avalonLogo: File = File("src/main/resources/images/avalonLogo.png")
val leaderCrown: File = File("src/main/resources/images/leaderCrown.jpg")

lateinit var steadfast: User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId, ktsId) = config

    bot(token) {
        steadfast = getUser(sfId)

        on(Ready) {
            //            getChannel(ktsId).send {
            steadfast.sendDM {
                title = "${this@bot.user.username} is logged on!!"
                color = neutral
                timestamp()
                url = "https://github.com/Andrew-Schwartz/AvalonBot"
            }
        }

        on(MessageDelete) {
            message(bot = this@bot).run {
                createDM(sfId).send {
                    title = "Message from ${this@run.author.username} deleted!"
                    if (content.isNotEmpty())
                        description = content
                    for (attachment in attachments) {
                        addField(attachment.filename, attachment.proxyUrl)
                    }
                }
            }
        }

        command(prefix = "!") {
            Command.run(this@bot, this, prefix)
        }
    }
}