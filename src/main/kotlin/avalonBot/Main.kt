package avalonBot

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.bot
import lib.dsl.onMessageCreate
import lib.dsl.onReady
import lib.misc.fromJson
import lib.model.Color
import lib.model.color
import lib.rest.http.httpRequests.getUser
import java.io.File

val config: ConfigJson = File("src/main/resources/config.json").readText().fromJson()

val neutral = "#BC9D46".color()
val good = "#3693D1".color()
val evil = "#BA4650".color()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId) = config

    bot(token) {
        onReady {
            //            println("${user.username} is logged on")
            getUser(sfId).sendDM {
                title = "${user.username} is logged on!!"
                color = neutral
                timestamp()
                author = user
            }
        }
        onMessageCreate {
            reply {
                title = "Embedded Title!!!"
                description = "Embedded description!!!"
                color = Color(255U, 0U, 0U)
                timestamp()
                author = getUser(sfId)
            }

//            for (command in commands) {
//                if (command.name == content.removePrefix(prefix).takeWhile { it != ' ' }) {
//                    command.execute(this@bot, this, content.split(" +".toRegex()).drop(1))
//                }
//            }
        }
    }
}
