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
        object Assassinate : Avalon()
    }

    sealed class Kittens : State() {
        object Game : Kittens()
    }

    sealed class Hangman : State() {
        object Game : Hangman()
    }

    fun name(): String = when (this) {
        All -> "All"
        is Setup -> "Setup"
        Game -> "Game"
        Avalon.Game -> "Avalon Game"
        Avalon.Quest -> "Avalon Quest"
        Avalon.Lady -> "Avalon Lady"
        Avalon.Assassinate -> "Avalon Assassinating"
        Avalon.Voting -> "Avalon Voting"
        Kittens.Game -> "Kittens Game"
        Hangman.Game -> "Hangman Game"
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

        return s1.orderNum().compareTo(s2.orderNum())
    }

    private fun State.orderNum(): Int {
        return when (this) {
            State.All -> 0
            is State.Setup -> 1
            State.Game -> 2
            is State.Avalon -> 3
            is State.Kittens -> 4
            is State.Hangman.Game -> 5
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