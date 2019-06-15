package avalonBot

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.bot
import lib.dsl.embed
import lib.dsl.onMessageCreate
import lib.dsl.onReady
import lib.misc.fromJson
import lib.rest.http.httpRequests.getUser
import java.io.File

val config: ConfigJson = File("src/main/resources/config.json").readText().fromJson()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId) = config

    bot(token) {
        onReady {
            //            println("${user.username} is logged on")
            getUser(sfId).sendDM(embed {
                title = "${user.username} is logged on!!"
                color = 0xBC9D46
                timestamp()
            })
        }
        onMessageCreate {
            reply(embed {
                title = "Embedded Title!!!"
                description = "Embedded description!!!"
                color = 0xBC9D46
                timestamp()
            })

//            for (command in commands) {
//                if (command.name == content.removePrefix(prefix).takeWhile { it != ' ' }) {
//                    command.execute(this@bot, this, content.split(" +".toRegex()).drop(1))
//                }
//            }
        }
    }
}
