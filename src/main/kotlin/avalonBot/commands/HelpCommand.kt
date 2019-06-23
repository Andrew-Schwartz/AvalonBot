package avalonBot.commands

import avalonBot.neutral
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message
import lib.util.bold
import lib.util.inlineCode
import lib.util.underline

object HelpCommand : Command {
    override val name: String
        get() = "help"

    override val description: String
        get() = "sends general help text, or specific help text if given a command name"

    override val usage: String
        get() = "!help [command name]".inlineCode()

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit
        get() = { message, args ->
            if (args.isEmpty()) {
                message.author.sendDM {
                    title = "List of commands".underline()
                    color = neutral
                    for (c in commands)
                        addField(c.name.bold(), c.description)
                }
            } else {
                val name = args[0]
                val command = commands.firstOrNull { it.name == name }

                if (command != null) {
                    message.author.sendDM {
                        title = "About ${command.name}".underline()
                        color = neutral
                        addField("Description", command.description, false)
                        addField("Usage", command.usage)
                    }
                } else {
                    message.channel.send("Unrecognized command. To learn more about a command, use $usage")
                }
            }
        }
}