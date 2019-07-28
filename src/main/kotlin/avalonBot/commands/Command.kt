package avalonBot.commands

import avalonBot.commands.CommandState.Setup
import avalonBot.commands.setup.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.S

@KtorExperimentalAPI
val commands: Set<Command> = S[
        HelpCommand,
        StartCommand,
        AddCommand,
        PlayersCommand,
        RolesCommand,
        ExitCommand,
        PingCommand,
        LadyCommand
]

enum class CommandState {
    Setup,
    AvalonGame
}

var currentState: CommandState = Setup

abstract class Command(vararg val states: CommandState) {
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
                if (command.name.equals(commandName, ignoreCase = true) && currentState in command.states) {
                    bot.run {
                        if (message.args.getOrNull(0) == "help" && command != HelpCommand)
                            message.reply(embed = command.helpEmbed())
                        else
                            command.execute(bot, message, message.args)
                    }
                }
        }
    }
}