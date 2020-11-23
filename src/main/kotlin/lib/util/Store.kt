@file:Suppress("NOTHING_TO_INLINE")

package lib.util

import lib.model.Snowflake
import lib.model.Storable

// todo should also store when it was last fetched, invalidate if it was too long ago
class Store<Id : Snowflake, T : Storable<Id, T>> {
    val map: MutableMap<Id, T> = mutableMapOf()
    val size: Int
        get() = map.size

    fun addOrUpdate(new: T): T {
        val id = new.intoId()
        return map.compute(id) { _, old ->
            old?.let {
                it.updateFrom(new)
                it
            } ?: new
        }!!
    }

    fun remove(id: Id) {
        map -= id
    }

//    inline operator fun minusAssign(id: Snowflake) = remove(id)
//
//    inline operator fun minusAssign(value: T) = remove(value.id)

    operator fun get(snowflake: Id): T? = map[snowflake]

    inline fun getOrPut(key: Id, default: () -> T): T = map.getOrPut(key, default)

    fun putIfAbsent(value: T): T = map.putIfAbsent(value.intoId(), value)!!

    suspend fun computeIfAbsent(id: Id, default: suspend () -> T): T =
            when (id) {
                in map -> map[id]!!
                else -> default().also { map[id] = it }
            }
}

//inline fun <Id : Snowflake, T : Storable<Id, T>> Store<Id, T>.forEach(action: (T) -> Unit) = map.forEach { action(it.value) }
//inline fun <Id : Snowflake, T : Storable<Id, T>> Store<Id, T>.all(predicate: (T) -> Boolean) = map.all { predicate(it.value) }
//inline fun <Id : Snowflake, T : Storable<Id, T>> Store<Id, T>.any(predicate: (T) -> Boolean) = map.any { predicate(it.value) }
//inline fun <Id : Snowflake, T : Storable<Id, T>> Store<Id, T>.none(predicate: (T) -> Boolean) = map.none { predicate(it.value) }