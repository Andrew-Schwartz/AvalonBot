package common.commands

import kotlin.reflect.KClass

sealed class State {
    object Setup : State()
    object All : State()

    sealed class Avalon : State() {
        object Game : Avalon()
        object Questing : Avalon()
        object Ladying : Avalon()
    }

    sealed class Kittens : State() {
        object Game : Kittens()
    }
}

inline fun <reified T : State> values() = values(T::class)

fun <T : State> values(kClass: KClass<T>): List<T> =
        kClass.sealedSubclasses.flatMap { subclass ->
            subclass.objectInstance
                    ?.let { listOf(it) }
                    ?: values(subclass)
        }