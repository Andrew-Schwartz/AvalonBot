package main.util

import explodingKittens.cards.Card
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.reflect.KClass

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

fun String.replaceCamelCase(with: String, makeLowerCase: Boolean = false): String = map {
    if (it.isUpperCase()) "$with${if (makeLowerCase) it.toLowerCase() else it}"
    else "${if (makeLowerCase) it.toLowerCase() else it}"
}.joinToString(separator = "")

suspend fun <T> T?.onNull(λ: suspend () -> Unit): T? {
    if (this == null) λ()
    return this
}

fun <T> List<T>.one(predicate: (T) -> Boolean): Boolean =
        fold(0) { num, t -> if (predicate(t)) num + 1 else num } == 1

fun <T> Array<T>.one(predicate: (T) -> Boolean): Boolean =
        fold(0) { num, t -> if (predicate(t)) num + 1 else num } == 1

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun List<String>.cards(): List<KClass<out Card>> {
    return (1..4).flatMap { i ->
        windowed(i)
                .map { it.joinToString(separator = "") }
                .mapNotNull { cardName ->
                    Card.cardCount.keys.firstOrNull {
                        it.simpleName!!.replaceCamelCase(" ").equals(cardName, true)
                    }
                }
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
operator fun <T : Card> List<T>.contains(cardClass: KClass<out T>): Boolean =
        any { it::class == cardClass }
