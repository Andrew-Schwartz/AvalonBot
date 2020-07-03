package hangman.game

import common.game.Setup
import common.game.State
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.RichEmbed
import lib.model.channel.Message

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class HangmanState(setup: Setup) : State<HangmanPlayer>(setup) {
    private val config = setup.config as HangmanConfig

    val randomWord = config.randomWord!!
    lateinit var word: String
    lateinit var revealed: MutableList<Char>
    val guesses = hashSetOf<Char>()

    var bodyParts = 0

    lateinit var message: Message
    val embed = RichEmbed()

    val asciiArt = mapOf(
            0 to """
                +-------------+
                |             |
                |
                |
                |
                |
                |
                |
                |
                |        +---------+
                |        |         |
                +--------+---------+--------+
                |                           |
            """.trimIndent(),
            1 to """
                +-------------+
                |             |
                |             O
                |
                |
                |
                |
                |
                |
                |        +---------+
                |        |         |
                +--------+---------+--------+
                |                           |
            """.trimIndent(),
            2 to """
                +-------------+
                |             |
                |             O
                |             |
                |             +                                
                |             |
                |             +
                |
                |
                |        +---------+
                |        |         |
                +--------+---------+--------+
                |                           |
            """.trimIndent(),
            3 to """
                +-------------+
                |             |
                |             O
                |           \ | /
                |            \+/
                |             |
                |             +
                |
                |
                |        +---------+
                |        |         |
                +--------+---------+--------+
                |                           |
            """.trimIndent(),
            4 to """
                +-------------+
                |             |
                |           \ O /
                |            \|/
                |             +
                |             |
                |             +
                |            / \
                |           /   \
                |        +---------+
                |        |         |
                +--------+---------+--------+
                |                           |
            """.trimIndent(),
            5 to """
                +-------------+
                |             |
                |           \ X /
                |            \|/
                |             +
                |             |
                |             +
                |            / \
                |           /   \
                |
                +---------------------------+
                |                           |
            """.trimIndent()
    )
}