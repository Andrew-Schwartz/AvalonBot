@file:Suppress("NOTHING_TO_INLINE")

package lib.util

@Suppress("UNCHECKED_CAST")
object A {
    inline operator fun <T> get(vararg vals: T): Array<T> = vals as Array<T>
}

object L {
    inline operator fun <T> get(vararg vals: T): List<T> = vals.asList()
}

object AL {
    inline operator fun <T> get(vararg vals: T): ArrayList<T> = arrayListOf(*vals)
}

object S {
    inline operator fun <T> get(vararg vals: T): Set<T> = vals.toSet()
}