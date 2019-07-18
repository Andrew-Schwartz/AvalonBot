package avalonBot

import avalonBot.characters.Character
import avalonBot.commands.Command
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.bot
import lib.dsl.command
import lib.dsl.on
import lib.model.User
import lib.model.color
import lib.rest.http.httpRequests.getChannel
import lib.rest.model.events.receiveEvents.Ready
import lib.util.fromJson
import java.io.File

val config: ConfigJson = File("src/main/resources/config.json").readText().fromJson()

val neutral = "#BC9D46".color()
val good = "#3693D1".color()
val evil = "#BA4650".color()

val players: MutableMap<String, User> = mutableMapOf()
val roles: ArrayList<Character> = ArrayList()

val avalonLogo: File = File("src/main/resources/images/avalonLogo.png")
val leaderCrown: File = File("src/main/resources/images/leaderCrown.jpg")

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId, ktsId) = config

    val ping = true
//    val ping = false

    bot(token) {
        on(Ready) {
            @Suppress("ConstantConditionIf")
            if (ping)
                getChannel(ktsId).send {
                    //                getUser(sfId).sendDM {
                    title = "${this@bot.user.username} is logged on!!"
                    color = neutral
                    timestamp()
                }
            else
                println("${user.username} is logged on")
        }

        command(prefix = "!") {
            Command.run(this@bot, this, prefix)
        }
    }
}