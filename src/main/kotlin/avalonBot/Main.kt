package avalonBot

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.bot
import java.io.File

val token = File("src/main/resources/token.txt").readText()
val commandPrefix = File("src/main/resources/prefix.txt").readText()
val steadfastId = File("src/main/resources/id.txt").readText()

const val api = "https://discordapp.com/api"

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
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
