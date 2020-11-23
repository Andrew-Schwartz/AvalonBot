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
import lib.util.pingNick

// TODO make me apply during games as well
object PlayersCommand : MessageCommand(All) {
    override val name: String = "players"

    override val description: String = "displays a list of all players currently in the game"

    override val usage: String = "players [ping]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val gamePlayers = GameType.values()
                .associate { it to Setup[message.channel(), it] }
                .mapValues { it.value.players }
                .filterValues { it.isNotEmpty() }

        when (gamePlayers.size) {
            0 -> message.channel().send("There are currently no players in any game")
            1 -> message.channel().send {
                val (game, players) = gamePlayers.entries.first()
                title = "Players in " + game.name.replaceCamelCase(" ")
                description = players.joinToString(separator = "\n") { it.user.pingNick() }
                color = Color.gold
            }
            else -> message.channel().send {
                color = Color.gold
                title = "Players"
                gamePlayers.forEach { (game, players) ->
                    addField(
                            game.name.replaceCamelCase(" "),
                            players.joinToString(separator = "\n") { it.user.pingNick() }
                    )
                }
            }
        }
    }
}