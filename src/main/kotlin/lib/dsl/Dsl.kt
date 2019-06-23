package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.Message
import lib.rest.model.events.receiveEvents.DispatchEvent
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.util.Action

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun <P> Bot.on(event: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    event.actions += λ
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun Bot.command(prefix: String = "", respondToBots: Boolean = false, λ: Action<Message>) {
    on(MessageCreate) {
        if (content.startsWith(prefix) && respondToBots || author.bot != true)
            λ()
    }
}