package lib.dsl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@ExperimentalCoroutinesApi
suspend fun blockUntil(millis: Long = 100, predicate: suspend () -> Boolean) {
    while (!predicate()) {
        delay(millis)
    }
}
