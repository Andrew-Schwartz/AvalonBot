package lib.dsl

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.rest.model.events.receiveEvents.DispatchEvent

/**
 * Whenever any Dispatch [events] are received from Discord's websocket, run [λ].
 *
 * Example:
 * ```kotlin
 * Bot(token) {
 *   on(MessageCreate, MessageUpdate) { /* this: Message */
 *    if (author.isBot != true) {
 *       reply("What a cool bot I am 🙂")
 *     }
 *   }
 * }
 * ```
 *
 * Takes [Bot] as to make the calls to this yellow :)
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun <P : Any> Bot.on(vararg events: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    events.forEach { it.actions += λ }
}

/**
 * Removes [λ] from the listeners on each of [events]. To use this, the [λ] used in [on] should be stored as a variable
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun <P : Any> Bot.off(vararg events: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    events.forEach { it.actions -= λ }
}
