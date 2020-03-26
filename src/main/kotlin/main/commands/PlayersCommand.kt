package main.commands

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import main.commands.CommandState.All
import main.game.GameType
import main.game.Setup
import main.game.name
import main.util.Colors
import main.util.replaceCamelCase

object PlayersCommand : Command(All) {
    override val name: String = "players"

    override val description: String = "displays a list of all players currently in the game"

    override val usage: String = """!players [ping]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val players = GameType.values()
                .associate { it to Setup[message.channel, it] }
                .mapValues { it.value.players }
                .filterValues { it.isNotEmpty() }

        when {
            players.isEmpty() -> message.reply("There are currently no players in any game")
            players.size == 1 -> message.reply {
                val (game, players) = players.entries.first()
                title = "Players in " + game.name.replaceCamelCase(" ")
                description = players.joinToString(separator = "\n") { it.name }
                color = Colors.gold
            }
            else -> message.reply {
                color = Colors.gold
                title = "Players"
                players.forEach { (game, players) ->
                    addField(
                            game.name.replaceCamelCase(" "),
                            players.joinToString(separator = "\n") { it.name }
                    )
                }
            }
        }
    }
}