package common.commands

import common.commands.State.All
import common.steadfast
import common.util.now
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin
import kotlin.system.exitProcess

object ExitCommand : Command(All) {
    override val name: String = "logoff"

    override val description: String = "logs this bot off"

    override val usage: String = "logoff"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        if (message.author == steadfast) {
            for (pin in pinnedMessages) {
                runCatching { deletePin(pin.channelId, pin.id) }
                        .onFailure { println(it.message) }
            }
            val logOff = "Logging off!"
            message.reply {
                title = logOff
                timestamp()
            }
            println("[${now()}] $logOff")
            websocket.close(CloseReason.Codes.NORMAL, "Exiting")
            exitProcess(1)
        } else {
            message.reply("Only Andrew is that cool")
        }
    }
}