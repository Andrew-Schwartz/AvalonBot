@file:Suppress("NOTHING_TO_INLINE")

package lib.util

import lib.model.Snowflake
import lib.model.Storable

class Store<T : Storable> {
    val map: MutableMap<Snowflake, T> = mutableMapOf()
    val size: Int
        get() = map.size

    fun add(value: T): T {
        return if (value.id in map) {
            map[value.id]!!.updateDataFrom(value) as T
//            value.updateDataFrom(map[value.id]!!)
        } else {
            value
        }.also { map[value.id] = it }
    }

//    inline operator fun plusAssign(value: T) = add(value)

    fun remove(id: Snowflake) {
        map -= id
    }

    inline operator fun minusAssign(id: Snowflake) = remove(id)

    inline operator fun minusAssign(value: T) = remove(value.id)

    operator fun get(snowflake: Snowflake): T? = map[snowflake]

    inline fun getOrPut(key: Snowflake, default: () -> T): T = map.getOrPut(key, default)

    fun putIfAbsent(value: T): T = map.putIfAbsent(value.id, value)!!

    //    suspend fun computeIfAbsent(id: Snowflake, default: suspend () -> T) = map.computeIfAbsent(id) { runBlocking { default() } }
    suspend fun computeIfAbsent(id: Snowflake, default: suspend () -> T): T {
        return if (id in map) {
            map[id]!!
        } else {
            default().also { map[id] = it }
        }
    }

    inline fun forEach(action: (T) -> Unit) = map.forEach { action(it.value) }

    inline fun all(predicate: (T) -> Boolean) = map.all { predicate(it.value) }
    inline fun any(predicate: (T) -> Boolean) = map.any { predicate(it.value) }
    inline fun none(predicate: (T) -> Boolean) = map.none { predicate(it.value) }

    inline fun first(predicate: (T) -> Boolean = { true }): T? {
        for ((_, v) in map) {
            if (predicate(v)) return v
        }
        return null
    }

    inline fun last(predicate: (T) -> Boolean = { true }): T? {
        for ((_, v) in map.entries.reversed()) {
            if (predicate(v)) return v
        }
        return null
    }
}
