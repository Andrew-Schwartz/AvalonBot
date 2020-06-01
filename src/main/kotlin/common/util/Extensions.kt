package common.util

import io.ktor.util.KtorExperimentalAPI
import kittens.cards.Card
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.reflect.KClass

operator fun String.get(range: IntRange): String = substring(range)

inline fun <A, R> Pair<A, A>.map(transform: (A) -> R): Pair<R, R> = transform(first) to transform(second)

fun <T> Iterable<T>.listGrammatically(stringify: (T) -> String = { it.toString() }): String {
    return map(stringify)
            .reduceIndexed { index, acc, name ->
                "$acc${
                if (index == count() - 1) "${if (index != 1) "," else ""} and"
                else ","
                } $name"
            }
}

fun <T> Array<T>.listGrammatically(stringify: (T) -> String = { it.toString() }): String {
    return asIterable().listGrammatically(stringify)
}

fun String.replaceCamelCase(with: String, makeLowerCase: Boolean = false): String = map {
    if (it.isUpperCase()) "$with${if (makeLowerCase) it.toLowerCase() else it}"
    else "${if (makeLowerCase) it.toLowerCase() else it}"
}.joinToString(separator = "")

// TODO non suspending version
suspend fun <T> T?.onNull(λ: suspend () -> Unit): T? {
    if (this == null) λ()
    return this
}

fun <T> not(predicate: (T) -> Boolean): (T) -> Boolean = { !predicate(it) }

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

fun <T> List<T>.getOrDefault(index: Int, default: T)
        : T =
        if (index in 0..lastIndex) get(index) else default
