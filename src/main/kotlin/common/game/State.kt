package common.game

import common.util.mapInstance
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
abstract class State<P : Player>(setup: Setup) {
    init {
        Setup.remove(setup)
    }

    val players: List<P> = setup.players.shuffled().mapInstance()

    @Suppress("LeakingThis")
    val userPlayerMap = players.associateBy { it.user }
}