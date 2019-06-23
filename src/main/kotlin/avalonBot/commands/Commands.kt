package avalonBot.commands

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message

val commands: ArrayList<Command> = arrayListOf(
        HelpCommand,
        StartCommand,
        AddCommand
)

interface Command {
    val name: String

    val description: String

    val usage: String

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    val execute: suspend Bot.(Message, args: List<String>) -> Unit
}