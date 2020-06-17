@file:Suppress("NOTHING_TO_INLINE")

package common.util

@Suppress("UNCHECKED_CAST")
object A {
    inline operator fun <T> get(vararg vals: T): Array<T> = vals as Array<T>
}

object L {
    inline operator fun <T> get(vararg vals: T): List<T> = vals.asList()
}

object ML {
    inline operator fun <T> get(vararg vals: T): MutableList<T> = vals.toMutableList()
}

object AL {
    inline operator fun <T> get(vararg vals: T): ArrayList<T> = arrayListOf(*vals)
}

object S {
    inline operator fun <T> get(vararg vals: T): Set<T> = vals.toSet()
}

object MS {
    inline operator fun <T> get(vararg vals: T): MutableSet<T> = vals.toMutableSet()
}

object M {
    inline operator fun <K, V> get(vararg entries: Pair<K, V>): Map<K, V> = entries.toMap()
}