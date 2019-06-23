package avalonBot

import avalonBot.commands.commands
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.bot
import lib.dsl.command
import lib.dsl.on
import lib.model.User
import lib.model.color
import lib.rest.http.httpRequests.getUser
import lib.rest.model.events.receiveEvents.Ready
import lib.util.fromJson
import java.io.File

val config: ConfigJson = File("src/main/resources/config.json").readText().fromJson()

val neutral = "#BC9D46".color()
val good = "#3693D1".color()
val evil = "#BA4650".color()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId) = config

    val ping = true
//    val ping = false

    bot(token) {
        on(Ready) {
            @Suppress("ConstantConditionIf")
            if (ping)
                getUser(sfId).sendDM {
                    title = "${this@bot.user.username} is logged on!!"
                    color = neutral
                    timestamp()
                }
            else
                println("${user.username} is logged on")
        }
        command {
            val commandName = content.removePrefix(prefix).takeWhile { it != ' ' }
            for (command in commands)
                if (command.name == commandName) {
                    val args = content.split(" +".toRegex()).drop(1)
                    command.execute(this@bot, this, args)
                }
        }
    }

    return@runBlocking
}