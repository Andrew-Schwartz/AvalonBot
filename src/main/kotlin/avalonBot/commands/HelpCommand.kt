package avalonBot.commands

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message

object HelpCommand : Command {
    override val name: String
        get() = "help"

    override val description: String
        get() = "sends general help text, or specific help text if given a command name"

    override val usage: String
        get() = "!help [command name]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit
        get() = { message, args ->
            if (args.isEmpty()) {
                message.reply(
                        "__List of Commands__\n" +
                                commands.joinToString(separator = "\n") { "**${it.name}**: ${it.description}" }
                )
            }
        }
}