package common.commands

import kotlin.reflect.KClass

/**
 * State machine nested "enum" for running commands
 */
sealed class State {
    object All : State()
    object Setup : State()

    sealed class Avalon : State() {
        object Game : Avalon()
        object Quest : Avalon()
        object Lady : Avalon()
    }

    sealed class Kittens : State() {
        object Game : Kittens()
    }

    fun name(): String = when (this) {
        All -> "All"
        Setup -> "Setup"
        Avalon.Game -> "Avalon Game"
        Avalon.Quest -> "Avalon Quest"
        Avalon.Lady -> "Avalon Lady"
        Kittens.Game -> "Kittens Game"
    }

    fun typeName(): String = name().takeWhile { it != ' ' }
}

/**
 * Sorts [Command]s by their state in the below order:
 *
 * [All]
 *
 * [Setup]
 *
 * [Avalon] (and sub-states)
 *
 * [Kittens] (and sub-states)
 */
object StateComparator : Comparator<Command> {
    override fun compare(o1: Command?, o2: Command?): Int {
        if (o1 == null && o2 == null) return 0
        val s1 = o1?.state ?: return 1
        val s2 = o2?.state ?: return -1
        return when (s1) {
            s2 -> 0
            State.All -> -1
            State.Setup -> when (s2) {
                State.All -> 1
                else -> -1
            }
            is State.Avalon -> when (s2) {
                State.All, State.Setup -> 1
                else -> -1
            }
            is State.Kittens -> 1
        }
    }
}

inline fun <reified T : State> subStates() = subStates(T::class)

fun <T : State> subStates(kClass: KClass<T>): List<T> =
        kClass.sealedSubclasses.flatMap { subclass ->
            subclass.objectInstance
                    ?.let { listOf(it) }
                    ?: subStates(subclass)
        }