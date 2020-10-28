package kittens.cards

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.util.bold

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Defuse(id: Int) : Card(id) {
    override val description: String = "Prevent an ${"Exploding Kitten".bold()} from exploding and put it back into the deck"

    override val playable = false
}