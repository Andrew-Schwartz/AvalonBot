package common.commands

import common.game.GameType
import common.game.Setup
import common.util.getOrDefault
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Color
import lib.model.channel.Message
import lib.util.inlineCode
import lib.util.pingReal
import lib.util.underline

// TODO add someone else by ping
object AddCommand : MessageCommand(State.Setup.Setup) {
    override val name: String = "addme"

    override val description: String = """
        adds player who sent this to a game. By default, you are added to Avalon,
        but you can specify ${"kittens".inlineCode()} to be added to Exploding Kittens
    """.trimIndent()

    override val usage: String = "addme [kittens]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val gameType = GameType.getType(message.args.getOrDefault(0, "")) ?: GameType.Avalon

        val setup = Setup[message.channel(), gameType]

        if (debug || message.author !in setup)
            setup.addPlayer(message.author)
        else
            setup.removePlayer(message.author)

        message.reply {
            color = Color.gold
            val playersList = setup.players
                    .joinToString(separator = "\n") { it.user.pingReal() }
                    .takeIf { it.isNotEmpty() }
                    ?: "None"
            addField("$gameType Current Players".underline(), playersList)
        }
    }
}