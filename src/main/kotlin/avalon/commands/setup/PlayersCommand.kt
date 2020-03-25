package avalon.commands.setup

import avalon.Colors
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.ping
import main.commands.Command
import main.commands.CommandState.AvalonGame
import main.commands.CommandState.Setup
import main.players

object PlayersCommand : Command(Setup, AvalonGame) {
    override val name: String = "players"

    override val description: String = "displays a list of all players currently in the game"

    override val usage: String = """!players ["ping"]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        when {
            players.isEmpty() -> message.reply("There are currently no players")
            else -> message.reply {
                color = Colors.neutral
                title = "Players"
                for ((nick, user) in players) {
                    addField(nick, user.ping(), inline = true)
                }
            }
        }
    }
}