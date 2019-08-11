package lib.dsl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@ExperimentalCoroutinesApi
suspend fun blockUntil(predicate: suspend () -> Boolean) {
    while (!predicate())
        delay(1000)
}
