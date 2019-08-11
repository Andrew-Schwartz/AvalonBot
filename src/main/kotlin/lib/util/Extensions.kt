package lib.util

operator fun String.get(range: IntRange): String = substring(range)

inline fun <A, R> Pair<A, A>.map(transform: (A) -> R): Pair<R, R> = transform(first) to transform(second)

fun <T> Iterable<T>.formatIterable(stringify: (T) -> String = { it.toString() }): String {
    return map(stringify)
            .reduceIndexed { index, acc, name ->
                "$acc${
                if (index == count() - 1) "${if (index != 1) "," else ""} and"
                else ","
                } $name"
            }
}

fun <T> Array<T>.formatIterable(stringify: (T) -> String = { it.toString() }): String {
    return asIterable().formatIterable(stringify)
}

suspend fun <T> T?.onNull(λ: suspend () -> Unit): T? {
    if (this == null) λ()
    return this
}