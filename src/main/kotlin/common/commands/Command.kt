package common.commands

import common.bot
import common.util.MS
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.Bot
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import org.reflections.Reflections

sealed class Command<P>(val state: State) {
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    abstract val execute: suspend Bot.(P) -> Unit

    companion object {
        val _currentStates: MutableMap<Channel, MutableSet<State>> = mutableMapOf()

        init {
            ReactCommand
            MessageCommand
        }
    }
}

abstract class MessageCommand(state: State) : Command<Message>(state) {
    abstract val name: String

    abstract val description: String

    abstract val usage: String

    companion object {
        val messageCommands = Reflections("").getSubTypesOf(MessageCommand::class.java)
                .mapNotNull { it.kotlin.objectInstance }
                .toSet()
                .also { messageCommands -> println("messageCommands = $messageCommands") }

        @KtorExperimentalAPI
        @ExperimentalCoroutinesApi
        suspend fun run(message: Message, prefix: String) {
            val commandName = message.content.removePrefix(prefix).takeWhile { it != ' ' }
            val channelStates = message.channel().states
            messageCommands.asSequence()
                    .filter { it.state in channelStates }
                    .filter { it.name.equals(commandName, true) }
                    .forEach { command ->
                        if (message.args.firstOrNull()?.equals("help", true) == true) {
                            GlobalScope.launch {
                                bot.run {
                                    message.reply(embed = command.helpEmbed())
                                }
                            }
                        } else {
                            command.execute(bot, message)
                        }
                    }
        }
    }
}

abstract class ReactCommand(state: State) : Command<MessageReactionUpdatePayload>(state) {
    /**
     * Emoji names that this command can be triggered by
     */
    abstract val emojis: List<String>

    companion object {
        val reactCommands = Reflections("").getSubTypesOf(ReactCommand::class.java)
                .mapNotNull { runCatching { it.kotlin.objectInstance }.getOrNull() }
                .toSet()
                .also { reactCommands -> println("reactCommands = $reactCommands") }

        @KtorExperimentalAPI
        @ExperimentalCoroutinesApi
        suspend fun run(reaction: MessageReactionUpdatePayload) {
            val channelStates = reaction.channel().states
            reactCommands.asSequence()
//                    .filter { it.state in channelStates }
                    .filter { reaction.emoji.name in it.emojis }
                    .forEach { it.execute(bot, reaction) }
        }
    }
}

val Channel.states: MutableSet<State>
    get() = Command._currentStates.getOrPut(this) { MS[State.All, State.Setup.Setup] }