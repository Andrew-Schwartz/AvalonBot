package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Message
import lib.rest.model.events.receiveEvents.DispatchEvent
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.util.Action

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun <P> Bot.on(event: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    event.actions += λ
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun <P> Bot.off(event: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    event.actions -= λ
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun Bot.command(prefix: String = "", respondToBots: Boolean = false, event: DispatchEvent<Message> = MessageCreate, λ: Action<Message>) {
    on(event) {
        if (content.startsWith(prefix) && respondToBots || author.isBot != true)
            λ()
    }
}