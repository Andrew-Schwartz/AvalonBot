package avalonBot

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.bot
import java.io.File

val config = File("src/main/resources/config.txt").readLines()

const val api = "https://discordapp.com/api"

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() {
    val (token, prefix, myId) = config

    bot(token) {
        //bot stuff here
    }
}

/*
suspend fun test() {
    bot(token) {
        commands(prefix = "!") {
            command("hi") {
                reply("yo")
            }
        }
    }
}
*/
