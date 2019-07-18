package avalonBot.commands

import avalonBot.commands.CommandState.AvalonGame
import avalonBot.commands.CommandState.General
import avalonBot.neutral
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.RichEmbed
import lib.dsl.embed
import lib.model.Message
import lib.util.bold
import lib.util.inlineCode
import lib.util.underline

@KtorExperimentalAPI
object HelpCommand : Command(General, AvalonGame) {
    override val name: String = "help"

    override val description: String = "sends general help text, or specific help text if given a command name"

    override val usage: String = "!help [command name]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val allCommandsEmbed: RichEmbed = embed {
            title = "List of commands".underline()
            color = neutral
            for (c in commands)
                addField(c.name.bold(), c.description)
        }

        if (args.isEmpty()) {
            message.author.sendDM(embed = allCommandsEmbed)
        } else {
            val name = args[0]
            val command = commands.firstOrNull { it.name == name }

            when {
                name == "here" -> message.reply(embed = allCommandsEmbed)
                command != null -> message.author.sendDM(embed = command.helpEmbed())
                else -> message.channel.send("Unrecognized command. To learn more about a command, use ${usage.inlineCode()}")
            }
        }
    }
}

@KtorExperimentalAPI
suspend fun Command.helpEmbed(): RichEmbed = embed {
    title = "About $name".underline()
    color = neutral
    addField("Description", this@helpEmbed.description, false)
    addField("Usage", usage.inlineCode())
}