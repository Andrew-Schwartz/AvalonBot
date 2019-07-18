package lib.util

operator fun String.get(range: IntRange): String = substring(range)

infix fun String.equalsIgnoreCase(other: String): Boolean = this.equals(other, true)
inline fun <A, R> Pair<A, A>.map(transform: (A) -> R): Pair<R, R> = transform(first) to transform(second)