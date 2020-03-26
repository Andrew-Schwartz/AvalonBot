package common.commands

import common.game.GameType
import common.game.Setup
import common.util.Colors
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.underline

object AddCommand : Command(CommandState.Setup) {
    override val name: String = "addme"

    override val description: String = """
        adds player who sent this to a game. By default, you are added to Avalon,
        but you can specify kittens to be added to Exploding Kittens
    """.trimIndent()

    override val usage: String = "!addme [kittens]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val gameType = if ("kittens" in args) GameType.ExplodingKittens else GameType.Avalon

        val setup = Setup[message.channel, gameType]

        if (message.author in setup)
            setup.removePlayer(message.author)
        else
            setup.addPlayer(message.author)

        message.reply {
            color = Colors.gold
            addField("Current Players".underline(), setup.players.joinToString(separator = "\n"))
        }
    }
}