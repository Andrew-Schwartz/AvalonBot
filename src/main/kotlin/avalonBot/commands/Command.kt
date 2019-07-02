package avalonBot.commands

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message
import lib.util.S

@KtorExperimentalAPI
val commands: Set<Command> = S[
        HelpCommand,
        StartCommand,
        AddCommand,
        PlayersCommand,
        RolesCommand,
        ExitCommand
]

abstract class Command {
    abstract val name: String

    abstract val description: String

    abstract val usage: String

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    abstract val execute: suspend Bot.(Message, args: List<String>) -> Unit

    companion object {
        @ExperimentalCoroutinesApi
        @KtorExperimentalAPI
        suspend fun run(bot: Bot, message: Message, prefix: String) {
            val commandName = message.content.removePrefix(prefix).takeWhile { it != ' ' }
            for (command in commands)
                if (command.name == commandName) {
                    bot.run {
                        val args = message.content.split(" +".toRegex()).drop(1)
                        if (args.getOrNull(0) == "help" && command != HelpCommand)
                            message.reply(embed = command.helpEmbed())
                        else
                            command.execute(bot, message, args)
                    }
                }
        }
    }
}