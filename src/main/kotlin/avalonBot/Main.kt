package avalonBot

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.bot
import lib.dsl.onMessage
import lib.dsl.onReady
import lib.misc.fromJson
import lib.rest.http.getUser
import java.io.File

val config: ConfigJson = File("src/main/resources/config.json").readText().fromJson()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId) = config

    bot(token) {
        onReady {
            bot.getUser(sfId.value).sendDM("${user.username} is logged on")
        }
        onMessage(prefix = "${prefix}test") {
            author.sendDM("Test DM")
        }
    }
}
