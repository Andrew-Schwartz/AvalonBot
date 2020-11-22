package common.util

import common.commands.DebugCommand
import io.ktor.util.*
import kittens.cards.Card
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.ChannelId
import lib.model.IntoId
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.math.pow
import kotlin.reflect.KClass

// General Java/Kotlin extensions

operator fun String.get(range: IntRange): String = substring(range)

inline fun <A, R> Pair<A, A>.map(transform: (A) -> R): Pair<R, R> = transform(first) to transform(second)

fun <T> Iterable<T>.listGrammatically(default: String = "", stringify: (T) -> String = { it.toString() }): String {
    val strings = map(stringify)
    return if (strings.isEmpty()) {
        default
    } else {
        strings.reduceIndexed { index, acc, name ->
            "$acc${
                if (index == count() - 1) "${if (index != 1) "," else ""} and"
                else ","
            } $name"
        }
    }
}

fun <T> Array<T>.listGrammatically(default: String = "", stringify: (T) -> String = { it.toString() }): String {
    return asIterable().listGrammatically(default, stringify)
}

fun String.replaceCamelCase(with: String, makeLowerCase: Boolean = false): String = map {
    if (it.isUpperCase()) "$with${if (makeLowerCase) it.toLowerCase() else it}"
    else "${if (makeLowerCase) it.toLowerCase() else it}"
}.joinToString(separator = "")

suspend fun <T> T?.onNull(λ: suspend () -> Unit): T? {
    if (this == null) λ()
    return this
}

fun <T> not(predicate: (T) -> Boolean): (T) -> Boolean = { !predicate(it) }

fun <T> List<T>.getOrDefault(index: Int, default: T): T =
        if (index in 0..lastIndex) get(index) else default

fun Instant.durationSince(start: Instant): Duration = Duration.between(start, this)

fun Instant.elapsed(): Duration = Instant.now().durationSince(this)

fun Int.pow(n: Int): Int = toDouble().pow(n).toInt()

// Extensions for common/lib packages

var IntoId<ChannelId>.debug: Boolean
    get() = DebugCommand.debug.getOrDefault(this.intoId(), false)
    set(value) {
        DebugCommand.debug[this.intoId()] = value
    }

@Suppress("UNCHECKED_CAST")
fun <T, R : T> Iterable<T>.mapInstance(): List<R> = map { it as R }

fun OffsetDateTime.elapsed(): Duration = toInstant().elapsed()

// Extensions for Exploding Kittens

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
