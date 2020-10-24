package common.commands

import common.util.MS
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import lib.dsl.Bot
import lib.dsl.channel
import lib.dsl.reply
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import org.reflections.Reflections

sealed class Command<P>(val state: State) {
    abstract val execute: suspend (P) -> Unit

    companion object {
        val channelStates: MutableMap<Channel, MutableSet<State>> = mutableMapOf()
    }
}

abstract class MessageCommand(state: State) : Command<Message>(state) {
    abstract val name: String

    open val aliases: List<(name: String) -> Boolean> = listOf()

    abstract val description: String

    abstract val usage: String

    companion object {
        val messageCommands = Reflections("")
                .getSubTypesOf(MessageCommand::class.java)
                .mapNotNull { it.kotlin.objectInstance }
                .toMutableSet()

        @KtorExperimentalAPI
        @ExperimentalCoroutinesApi
        suspend fun run(message: Message, prefix: String) {
            val commandName = message.content.removePrefix(prefix).takeWhile { it != ' ' }
            val channelStates = message.channel().states
            messageCommands
                    .toList() // copy in case list is modified
                    .asSequence()
                    .filter { it.state in channelStates }
                    .filter { it.name.equals(commandName, true) || it.aliases.any { it(commandName) } }
                    .forEach { command ->
                        Bot.launch {
                            if (message.args.firstOrNull()?.equals("help", true) == true) {
                                message.reply(embed = command.helpEmbed())
                            } else {
                                command.execute(message)
                            }
                        }
                    }
        }

        fun addCommand(command: MessageCommand, channel: Channel) {
            messageCommands += command
            channelStates.getOrPut(channel) { mutableSetOf() } += command.state
        }

        fun removeCommand(command: MessageCommand, channel: Channel) {
            messageCommands -= command
            channelStates[channel]?.remove(command.state)
        }
    }
}

abstract class ReactCommand(state: State) : Command<MessageReactionUpdatePayload>(state) {
    /**
     * Emoji names that this command can be triggered by
     */
    abstract val emojis: List<String>

    companion object {
        val reactCommands = Reflections("")
                .getSubTypesOf(ReactCommand::class.java)
                .mapNotNull { runCatching { it.kotlin.objectInstance }.getOrNull() }
                .toMutableSet()

        @KtorExperimentalAPI
        @ExperimentalCoroutinesApi
        suspend fun run(reaction: MessageReactionUpdatePayload) {
            val channelStates = reaction.channel().states

            reactCommands
                    .toList() // copy in case list is modified
                    .asSequence()
                    .filter { reaction.emoji.name in it.emojis }
                    .filter { it.state in channelStates }
                    .forEach { Bot.launch { it.execute(reaction) } }
        }

        fun addCommand(command: ReactCommand, channel: Channel) {
            reactCommands += command
            channelStates.getOrPut(channel) { mutableSetOf() } += command.state
        }

        fun removeCommand(command: ReactCommand, channel: Channel) {
            reactCommands -= command
            channelStates[channel]?.remove(command.state)
        }
    }
}

val Channel.states: MutableSet<State>
    get() = Command.channelStates.getOrPut(this) { MS[State.All, State.Setup.Setup] }