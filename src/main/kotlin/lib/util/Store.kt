package lib.util

import lib.model.Snowflake
import lib.model.Storable

class Store<T : Storable> {
    val map: MutableMap<Snowflake, T> = mutableMapOf()

    operator fun plusAssign(value: T) {
        map[value.id] = value
    }

    operator fun minusAssign(value: T) {
        map -= value.id
    }

    operator fun minusAssign(key: Snowflake) {
        map -= key
    }

    operator fun get(snowflake: Snowflake): T? = map[snowflake]

    inline fun getOrPut(key: Snowflake, default: () -> T): T = map.getOrPut(key, default)

    fun putIfAbsent(value: T): T = map.putIfAbsent(value.id, value)!!
}