package common.commands

import common.bot
import common.commands.CommandState.Setup
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Channel
import lib.model.channel.Message
import org.reflections.Reflections

enum class CommandState {
    Setup,
    AvalonGame,
    KittensGame,
    All
}

abstract class Command(vararg val states: CommandState) { // TODO this is just one state and each channel can be in multiple states
    abstract val name: String

    abstract val description: String

    abstract val usage: String

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    abstract val execute: suspend Bot.(Message, args: List<String>) -> Unit

    companion object {
        val commandSet = Reflections("").getSubTypesOf(Command::class.java)
                .mapNotNull { it.kotlin.objectInstance }
                .toMutableSet()

        val currentStates: MutableMap<Channel, CommandState> = mutableMapOf()

        @KtorExperimentalAPI
        @ExperimentalCoroutinesApi
        suspend fun run(message: Message, prefix: String) {
            val commandName = message.content.removePrefix(prefix).takeWhile { it != ' ' }
            for (command in commandSet.toList()) { // hopefully this fixes the ConcurrentModificationException
                if (command.name.equals(commandName, ignoreCase = true) &&
                        (with(bot) { message.channel.commandState } in command.states || // TODO switch the order
                                CommandState.All in command.states)
                ) {
                    bot.run {
                        if (message.args.getOrNull(0) == "help"/* && command != HelpCommand*/)
                            message.reply(embed = command.helpEmbed())
                        else
                            command.execute(bot, message, message.args)
                    }
                }

            }
        }
    }
}

var Channel.commandState: CommandState
    get() = Command.currentStates.getOrPut(this) { Setup }
    set(value) {
        Command.currentStates[this] = value
    }