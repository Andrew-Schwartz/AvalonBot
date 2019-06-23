package avalonBot.commands

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message

val commands: Set<Command> = setOf(
        HelpCommand,
        StartCommand,
        AddCommand,
        PlayersCommand,
        RolesCommand
)

abstract class Command {
    abstract val name: String

    abstract val description: String

    abstract val usage: String

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    abstract val execute: suspend Bot.(Message, args: List<String>) -> Unit

    companion object {
        @KtorExperimentalAPI
        @ExperimentalCoroutinesApi
        suspend fun run(bot: Bot, message: Message, prefix: String) {
            val commandName = message.content.removePrefix(prefix).takeWhile { it != ' ' }
            for (command in commands)
                if (command.name == commandName) {
                    val args = message.content.split(" +".toRegex()).drop(1)
                    command.execute(bot, message, args)
                }
        }
    }
}