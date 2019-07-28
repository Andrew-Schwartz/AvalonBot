@file:Suppress("NOTHING_TO_INLINE")

package lib.util

import lib.model.Snowflake
import lib.model.Storable

class Store<T : Storable> {
    val map: MutableMap<Snowflake, T> = mutableMapOf()

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
}