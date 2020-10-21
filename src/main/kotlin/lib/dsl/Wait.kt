package lib.dsl

import kotlinx.coroutines.delay

/**
 * Used internally to delay until [predicate] is true, waiting [millis] every time it isn't.
 */
suspend fun suspendUntil(millis: Long = 100, predicate: suspend () -> Boolean) {
    while (!predicate()) {
        delay(millis)
    }
}
