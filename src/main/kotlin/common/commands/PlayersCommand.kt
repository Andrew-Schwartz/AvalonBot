package common.commands

import common.commands.State.All
import common.game.GameType
import common.game.Setup
import common.util.replaceCamelCase
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.send
import lib.model.Color
import lib.model.channel.Message

// TODO fix me cuz I don't work at all lmao
object PlayersCommand : MessageCommand(All) {
    override val name: String = "players"

    override val description: String = "displays a list of all players currently in the game"

    override val usage: String = "players [ping]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val players = GameType.values()
                .associate { it to Setup[message.channel(), it] }
                .mapValues { it.value.players }
                .filterValues { it.isNotEmpty() }

        when {
            players.isEmpty() -> message.channel().send("There are currently no players in any game")
            players.size == 1 -> message.channel().send {
                val (game, players) = players.entries.first()
                title = "Players in " + game.name.replaceCamelCase(" ")
                description = players.joinToString(separator = "\n") { it.name }
                color = Color.gold
            }
            else -> message.channel().send {
                color = Color.gold
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