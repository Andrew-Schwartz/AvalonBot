package lib.dsl

import common.util.L
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.Activity
import lib.rest.model.events.receiveEvents.DispatchEvent
import lib.rest.model.events.sendEvents.Status
import lib.rest.model.events.sendEvents.UpdateStatus
import java.time.Instant

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
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun <P : Any> Bot.on(vararg events: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    events.forEach { it.actions += λ }
}

/**
 * Removes [λ] from the listeners on each of [events]. To use this, the [λ] used in [on] should be stored as a variable
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun <P : Any> Bot.off(vararg events: DispatchEvent<P>, λ: suspend P.() -> Unit) {
    events.forEach { it.actions -= λ }
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Bot.updateStatus(activity: Activity, status: Status) {
    websocket!!.sendGatewayEvent(UpdateStatus(L[activity], status, false, Instant.now().toEpochMilli()))
}