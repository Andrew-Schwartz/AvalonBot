package lib.dsl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

val eventListeners: ArrayList<Pair<suspend () -> Boolean, suspend () -> Unit>> = arrayListOf()

@ExperimentalCoroutinesApi
suspend fun blockUntil(pauseTime: Long = 500, predicate: suspend () -> Boolean) {
    while (!predicate())
        delay(pauseTime)
}

@ExperimentalCoroutinesApi
suspend fun blockTime(millis: Long, pauseTime: Long = if (millis < 30000) 200 else 500) {
    val startTime = System.currentTimeMillis()
    blockUntil(pauseTime) { System.currentTimeMillis() - startTime >= millis }
}