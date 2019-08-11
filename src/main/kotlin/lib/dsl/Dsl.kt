package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Message
import lib.rest.model.events.receiveEvents.DispatchEvent
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.util.A
import lib.util.Action

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun <P> Bot.on(vararg events: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    events.forEach { it.actions += λ }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun <P> Bot.off(vararg events: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    events.forEach { it.actions -= λ }
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