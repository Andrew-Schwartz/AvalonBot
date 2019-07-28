package avalonBot.commands

import avalonBot.commands.CommandState.AvalonGame
import avalonBot.commands.CommandState.Setup
import avalonBot.steadfast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.exceptions.RequestException
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin
import kotlin.system.exitProcess

object ExitCommand : Command(Setup, AvalonGame) {
    override val name: String = "logoff"

    override val description: String = "logs this bot off"

    override val usage: String = "!logoff"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        when (message.author) {
            steadfast -> {
                for (pin in pinnedMessages) {
                    try {
                        deletePin(pin.channelId, pin.id)
                    } catch (e: RequestException) {
                        println(e.message)
                    }
                }
                val msg = "Logging off!"
                message.reply {
                    title = msg
                    timestamp()
                }
                println(msg)
                exitProcess(1)
            }
            else -> {
                message.author.sendDM("Only Andrew is special")
            }
        }
    }
}