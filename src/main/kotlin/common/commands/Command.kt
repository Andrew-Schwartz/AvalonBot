package common.commands

import common.bot
import common.commands.State.All
import common.commands.State.Setup
import common.util.MS
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Channel
import lib.model.channel.Message
import org.reflections.Reflections

abstract class Command(val state: State) {
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

        val _currentStates: MutableMap<Channel, MutableSet<State>> = mutableMapOf()

        @KtorExperimentalAPI
        @ExperimentalCoroutinesApi
        suspend fun run(message: Message, prefix: String) {
            val commandName = message.content.removePrefix(prefix).takeWhile { it != ' ' }
            for (command in commandSet.toList()) { // hopefully this fixes the ConcurrentModificationException
                if (command.name.equals(commandName, ignoreCase = true) &&
                        (with(bot) { command.state in message.channel.states })
                ) {
                    bot.run {
                        if (message.args.getOrNull(0) == "help")
                            message.reply(embed = command.helpEmbed())
                        else
                            command.execute(bot, message, message.args)
                    }
                }
            }
        }
    }
}

val Channel.states: MutableSet<State>
    get() = Command._currentStates.getOrPut(this) { MS[All, Setup] }
//    set(value) {
//        Command._currentStates[this] = value
//    }