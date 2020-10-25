@file:Suppress("NOTHING_TO_INLINE")

package lib.util

import lib.model.Snowflake
import lib.model.Storable

class Store<T : Storable<T>> {
    val map: MutableMap<Snowflake, T> = mutableMapOf()
    val size: Int
        get() = map.size

    fun addOrUpdate(new: T): T {
        val id = new.id
        return map.compute(id) { _, old ->
            old?.let {
                it.updateFrom(new)
                it
            } ?: new
        }!!
    }

    fun remove(id: Snowflake) {
        map -= id
    }

//    inline operator fun minusAssign(id: Snowflake) = remove(id)
//
//    inline operator fun minusAssign(value: T) = remove(value.id)

    operator fun get(snowflake: Snowflake): T? = map[snowflake]

    inline fun getOrPut(key: Snowflake, default: () -> T): T = map.getOrPut(key, default)

    fun putIfAbsent(value: T): T = map.putIfAbsent(value.id, value)!!

    suspend fun computeIfAbsent(id: Snowflake, default: suspend () -> T): T =
            when (id) {
                in map -> map[id]!!
                else -> default().also { map[id] = it }
            }
}

inline fun <T : Storable<T>> Store<T>.forEach(action: (T) -> Unit) = map.forEach { action(it.value) }
inline fun <T : Storable<T>> Store<T>.all(predicate: (T) -> Boolean) = map.all { predicate(it.value) }
inline fun <T : Storable<T>> Store<T>.any(predicate: (T) -> Boolean) = map.any { predicate(it.value) }
inline fun <T : Storable<T>> Store<T>.none(predicate: (T) -> Boolean) = map.none { predicate(it.value) }

// what do first and last mean though this is a map???
inline fun <T : Storable<T>> Store<T>.first(predicate: (T) -> Boolean = { true }): T? {
    for ((_, v) in map) {
        if (predicate(v)) return v
    }
    return null
}

inline fun <T : Storable<T>> Store<T>.last(predicate: (T) -> Boolean = { true }): T? {
    for ((_, v) in map.entries.reversed()) {
        if (predicate(v)) return v
    }
    return null
}
