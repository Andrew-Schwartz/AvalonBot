package common.commands

import kotlin.reflect.KClass

/**
 * State machine nested "enum" for running commands
 */
sealed class State {
    object All : State()

    sealed class Setup : State() {
        object Setup : State.Setup()
        object AvalonStart : State.Setup()
        object KittensStart : State.Setup()
    }

    object Game : State()

    sealed class Avalon : State() {
        object Game : Avalon()
        object Quest : Avalon()
        object Lady : Avalon()
        object Voting : Avalon()
    }

    sealed class Kittens : State() {
        object Game : Kittens()
    }

    fun name(): String = when (this) {
        All -> "All"
        is Setup -> "Setup"
        Game -> "Game"
        Avalon.Game -> "Avalon Game"
        Avalon.Quest -> "Avalon Quest"
        Avalon.Lady -> "Avalon Lady"
        Avalon.Voting -> "Avalon Voting"
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
 * [Game]
 *
 * [Avalon] (and sub-states)
 *
 * [Kittens] (and sub-states)
 */
object StateComparator : Comparator<MessageCommand> {
    override fun compare(o1: MessageCommand?, o2: MessageCommand?): Int {
        if (o1 == null && o2 == null) return 0
        val s1 = o1?.state ?: return 1
        val s2 = o2?.state ?: return -1
        return when (s1) {
            s2 -> 0
            State.All -> -1
            is State.Setup -> when (s2) {
                State.All -> 1
                else -> -1
            }
            State.Game -> when (s2) {
                State.All, is State.Setup -> 1
                else -> -1
            }
            is State.Avalon -> when (s2) {
                State.All, is State.Setup, State.Game -> 1
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