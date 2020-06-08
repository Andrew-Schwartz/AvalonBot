package kittens.cards

import common.bot
import common.util.replaceCamelCase
import io.ktor.util.KtorExperimentalAPI
import kittens.game.ExplodingKittens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.util.bold
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
abstract class Card(val id: Int) {
    val name = this::class.simpleName!!.replaceCamelCase(" ", false)

    abstract val description: String

    open val playable: Boolean = true

    private val imgDir = File("src/main/resources/images/exploding_kittens/${this::class.simpleName!!.replaceCamelCase("_", true)}")

    val image: File
        get() = imgDir.listFiles { _, name -> "$id" in name }!!.first()

    open suspend fun ExplodingKittens.play() {}

    open suspend fun ExplodingKittens.draw() {
        val player = state.currentPlayer
        with(bot) {
            player.user.sendDM {
                title = "You drew ${name.bold()}"
                image(image)
                description = "Your hand is now ${player.hand.joinToString(separator = "\n")}"
            }
        }
    }

    override fun toString() = name

    companion object {
        val cardCount: MutableMap<KClass<out Card>, Int> = mutableMapOf()

        inline fun <reified C : Card> makeCard(): C {
            val id = cardCount.putIfAbsent(C::class, 0) ?: 0
            return C::class.primaryConstructor!!.call(id)
        }
    }
}
