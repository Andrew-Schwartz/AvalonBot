package avalonBot

import discord4j.core.DiscordClient
import discord4j.core.event.domain.Event
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.bot
import lib.dsl.embed
import lib.dsl.onMessageCreate
import lib.dsl.onReady
import lib.model.color
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.getUser
import lib.rest.http.httpRequests.sendImage
import lib.util.fromJson
import reactor.core.Disposable
import java.io.File
import java.util.*

val config: ConfigJson = File("src/main/resources/config.json").readText().fromJson()

val neutral = "#BC9D46".color()
val good = "#3693D1".color()
val evil = "#BA4650".color()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId) = config

//    val ping = true
    val ping = false

    bot(token) {
        onReady {
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
        onMessageCreate {
            sendImage(channel, CreateMessage(
                    embed = embed {
                        title = "Sending in an embed"
                        field("field", "value")
                    }
//                    file = File("src/main/resources/images/avalon_logo.png").readBytes()
            ))

//            val commandName = content.removePrefix(prefix).takeWhile { it != ' ' }
//            for (command in commands)
//                if (command.name == commandName) {
//                    val args = content.split(" +".toRegex()).drop(1)
//                    command.execute(this@bot, this, args)
//                }
        }
    }

    return@runBlocking
}

//fun main() {
//    val (token, prefix, sfId) = config
//
//    val client = DiscordClientBuilder(token).build()
//
//    client.on<ReadyEvent> { event ->
//        val self = event.self
//        println("logged in as ${self.username}")
//    }
////            .subscribe { event ->
////                val self = event.self
////                println("logged in as ${self.username}")
////            }
//
//    client.on<MessageCreateEvent> {
//        val message = it.message.takeUnless { it.author.orNull()?.isBot ?: false }
//        val channel = message?.channel
//        channel?.block()?.createMessage {
//
//        }
////        val channel = message.author.filter { !it.isBot }
//
////        val author = message.author.map { !it.isBot }.orElse(false)
//    }
//
//    client.eventDispatcher.on(MessageCreateEvent::class.java)
//            .map { it.message }
//            .filter { it.author.map { !it.isBot }.orElse(false) }
//            .flatMap { it.channel }
//            .flatMap {
//                it.createMessage {
//                    it.addFile("avalon_logo.png", FileInputStream("src/main/resources/images/avalon_logo.png"))
//                }
//            }
//            .subscribe()
//
//    client.login().block()
//}

private fun <T> Optional<T>.orNull(): T? = orElse(null)

inline fun <reified T : Event> DiscordClient.on(noinline λ: (T) -> Unit): Disposable {
    return eventDispatcher.on(T::class.java).subscribe(λ)
}