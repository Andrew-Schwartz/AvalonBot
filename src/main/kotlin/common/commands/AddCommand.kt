package common.commands

import common.game.GameType
import common.game.Setup
import common.util.Colors
import common.util.getOrDefault
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.inlineCode
import lib.util.pingReal
import lib.util.underline

object AddCommand : Command(State.Setup) {
    override val name: String = "addme"

    override val description: String = """
        adds player who sent this to a game. By default, you are added to Avalon,
        but you can specify ${"kittens".inlineCode()} to be added to Exploding Kittens
    """.trimIndent()

    override val usage: String = "addme [kittens]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val gameType = GameType.getType(args.getOrDefault(0, "")) ?: GameType.Avalon

        val setup = Setup[message.channel, gameType]

        if (!debug && message.author in setup)
            setup.removePlayer(message.author)
        else
            setup.addPlayer(message.author) // todo validate size

        message.reply {
            color = Colors.gold
            val playersList = setup.players
                    .joinToString(separator = "\n") { it.user.pingReal() }
                    .takeIf { it.isNotEmpty() }
                    ?: "None"
            addField("Current Players".underline(), playersList)
        }
    }
}