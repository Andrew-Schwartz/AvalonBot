package common.commands

import common.commands.State.All
import common.steadfast
import common.util.now
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.reply
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin
import kotlin.system.exitProcess

object ExitCommand : MessageCommand(All) {
    override val name: String = "logoff"

    override val description: String = "logs this bot off"

    override val usage: String = "logoff"

    override val privileged: Boolean = true

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        if (message.author == steadfast) {
            for (pin in Bot.pinnedMessages) {
                runCatching { deletePin(pin.channelId, pin) }
                        .onFailure { println(it.message) }
            }
            val logOff = "Logging off!"
            message.reply {
                title = logOff
                timestamp()
            }
            println("[${now()}] $logOff")
            Bot.websocket?.close?.invoke(CloseReason.Codes.NORMAL, "Exiting")
            exitProcess(1)
        } else {
            message.reply("Only Andrew is that cool")
        }
    }
}