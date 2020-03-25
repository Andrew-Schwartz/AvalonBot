package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Message
import lib.rest.model.events.receiveEvents.DispatchEvent
import lib.rest.model.events.receiveEvents.MessageCreate
import main.commands.Command
import main.util.A
import main.util.Action
import main.util.Listener

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun <P> Bot.on(vararg events: DispatchEvent<P>, λ: Listener<P>) {
    events.forEach { it.actions += λ }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun Bot.on(command: Command) {
    Command.commandSet += command
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun <P> Bot.off(vararg events: DispatchEvent<P>, λ: Listener<P>) {
    events.forEach { it.actions -= λ }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun Bot.off(command: Command) {
    Command.commandSet -= command
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun Bot.command(prefix: String = "", respondToBots: Boolean = false, vararg events: DispatchEvent<Message> = A[MessageCreate], λ: Action<Message>) {
    on(*events) {
        if (content.startsWith(prefix) && (respondToBots || author.isBot != true)) {
            λ()
        }
    }
}