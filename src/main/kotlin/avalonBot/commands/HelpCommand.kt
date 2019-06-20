package avalonBot.commands

import avalonBot.neutral
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.RichEmbed
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
            val embed = RichEmbed(color = neutral)

            if (args.isEmpty()) {
                message.author.sendDM(embed) {
                    title = "List of commands".underline()
                    for (c in commands)
                        field(c.name.bold(), c.description)
                }
            } else {
                val name = args[0]
                val command = commands.firstOrNull { it.name == name }

                if (command != null) {
                    message.author.sendDM(embed) {
                        title = "About ${command.name}".underline()
                        field("Description", command.description, false)
                        field("Usage", command.usage)
                    }
                } else {
                    message.channel.send("Unrecognized command. To learn more about a command, use $usage")
                }
            }
        }
}