package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

val eventListeners: ArrayList<Pair<suspend () -> Boolean, suspend () -> Unit>> = arrayListOf()

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun blockUntil(predicate: suspend () -> Boolean) {
    while (!predicate())
        delay(500)
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun Bot.waitUntil(predicate: suspend () -> Boolean, λ: suspend () -> Unit) {
    eventListeners += predicate to λ
}

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun Bot.waitTime(millis: Long, λ: suspend () -> Unit) {
    val startTime = System.currentTimeMillis()
    val predicate = suspend { System.currentTimeMillis() - startTime >= millis }
    waitUntil(predicate, λ)
}