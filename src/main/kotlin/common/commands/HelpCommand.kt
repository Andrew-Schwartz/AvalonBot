package common.commands

import common.steadfast
import common.util.A
import common.util.debug
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.*
import lib.model.Color
import lib.model.channel.Message
import lib.util.bold
import lib.util.inlineCode
import lib.util.underline

object HelpCommand : MessageCommand(State.All) {
    override val name: String = "help"

    override val description: String = "sends setup help text, or specific help text if given a command name, such" +
            "as ${"addme".inlineCode()}. Parameters documented by ${"usage".inlineCode()} are required if surrounded" +
            "by angle brackets <...> and optional if surrounded by square bracktes [...]"

    override val usage: String = "help [command (type)]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val allCommandsEmbed: suspend () -> RichEmbed = {
            embed {
                title = "List of commands".underline()
                color = Color.gold
                messageCommands.filter { message.channelId.debug || it.state in message.channel().states }
                        .filter { message.channelId.debug || message.author == steadfast || it !in A[ExitCommand, LogCommand, DebugCommand] }
                        .sortedWith(StateComparator)
                        .forEach {
                            val name = "▶ ${it.state.typeName().toLowerCase()} - ${it.name}".bold()
                            addField(name, it.description)
                        }
            }
        }
        val args = message.args
        if (args.isEmpty()) {
            message.author.sendDM(embed = allCommandsEmbed()) // DM cuz its long
        } else {
            val name = args[0].toLowerCase()
            val command = messageCommands.firstOrNull { it.name == name }

            when {
                name == "here" -> message.reply(embed = allCommandsEmbed())
                command != null -> message.reply(embed = command.helpEmbed())
                else -> message.reply("Unrecognized command. To learn more about a command, use ${"!$usage".inlineCode()}")
            }
        }
    }
}

suspend fun MessageCommand.helpEmbed(): RichEmbed = embed {
    title = "About $name".underline()
    color = Color.gold
    addField("Description", this@helpEmbed.description, false)
    addField("Usage", "!${usage}".inlineCode())
    addField("Usable for", state.name())
}