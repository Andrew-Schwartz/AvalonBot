package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.rest.model.events.receiveEvents.DispatchEvent

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
