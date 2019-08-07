package lib.dsl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@ExperimentalCoroutinesApi
suspend fun blockUntil(pauseTime: Long = 500, predicate: suspend () -> Boolean) {
    while (!predicate())
        delay(pauseTime)
}
