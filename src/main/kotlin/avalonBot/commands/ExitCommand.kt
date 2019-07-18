package avalonBot.commands

import avalonBot.commands.CommandState.AvalonGame
import avalonBot.commands.CommandState.General
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message
import kotlin.system.exitProcess

object ExitCommand : Command(General, AvalonGame) {
    override val name: String = "logoff"

    override val description: String = "logs this bot off"

    override val usage: String = "!logoff"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val msg = "Logging off!"
        message.reply(msg)
        println(msg)
        exitProcess(1)
    }
}