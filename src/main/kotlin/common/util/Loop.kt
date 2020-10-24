package common.util

/**
 * the backend for [loop]
 */
class Loop<T> internal constructor(val action: suspend Loop<T>.() -> T?) {
    private var retVal: T? = null

    suspend fun run(): T {
        while (retVal == null) {
            retVal = action()
        }
        return retVal!!
    }
}

/**
 * Modeled after Rust's loop construct. Use as follows
 * ```kotlin
 * var count = 0
 * val num: Int = loop {
 *     count++
 *     if (count == 3) {
 *         println("three")
 *         return@loop null // Rust's continue
 *     }
 *     println(count)
 *     if (count == 5) {
 *         println("that's enough")
 *         return@loop 42 // Rust's break 42
 *     }
 *     null
 * }
 * assert(num == 42)
 * ```
 * `continue` -> `return@loop null`
 *
 * `break value` -> `return@loop value`
 *
 * Must end with `null`, cannot return null values
 */
suspend fun <T> loop(action: suspend Loop<T>.() -> T?): T {
    return Loop(action).run()
}