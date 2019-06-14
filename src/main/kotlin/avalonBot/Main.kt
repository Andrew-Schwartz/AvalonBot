package avalonBot

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.dsl.bot
import lib.dsl.onGuildCreate
import lib.dsl.onMessageCreate
import lib.dsl.onReady
import lib.misc.fromJson
import java.io.File

val config: ConfigJson = File("src/main/resources/config.json").readText().fromJson()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val (token, prefix, sfId) = config

    bot(token) {
        onReady {
            println("${user.username} is logged on")
//            getUser(sfId).sendDM("${getUser.username} is logged on")

        }
        onMessageCreate(prefix = prefix) {
            println(channelId)
        }
        onGuildCreate {
            channels
                    ?.filter { it.type }
                    ?.forEach { channel ->
                        println("${channel.name} ${channel.id}")
//                channel.lastMessageId?.let {
//                    val lastMessage = getMessage(channel.id, it)
//                    val messages = getMessages(GetChannelMessages.before(channel.id, lastMessage.id))
//                    for (message in messages)
//                        println(message.content)
//                }
                    }
        }
    }
}
