package common.commands

import common.commands.State.All
import common.util.Colors
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.RichEmbed
import lib.dsl.embed
import lib.model.channel.Message
import lib.util.bold
import lib.util.inlineCode
import lib.util.underline

object HelpCommand : Command(All) {
    override val name: String = "help"

    override val description: String = "sends setup help text, or specific help text if given a command name"

    override val usage: String = "!help [command name]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val allCommandsEmbed: RichEmbed = embed {
            title = "List of commands".underline()
            color = Colors.gold
            for (c in commandSet)
                addField(c.name.bold(), c.description)
        }

        if (args.isEmpty()) {
            message.author.sendDM(embed = allCommandsEmbed)
        } else {
            val name = args[0]
            val command = commandSet.firstOrNull { it.name == name }

            when {
                name == "here" -> message.reply(embed = allCommandsEmbed)
                command != null -> message.author.sendDM(embed = command.helpEmbed())
                else -> message.channel.send("Unrecognized command. To learn more about a command, use ${usage.inlineCode()}")
            }
        }
    }
}

suspend fun Command.helpEmbed(): RichEmbed = embed {
    title = "About $name".underline()
    color = Colors.gold
    addField("Description", this@helpEmbed.description, false)
    addField("Usage", usage.inlineCode())
}