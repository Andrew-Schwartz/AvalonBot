package avalonBot.commands.setup

import avalonBot.Colors
import avalonBot.commands.Command
import avalonBot.commands.CommandState.AvalonGame
import avalonBot.commands.CommandState.Setup
import avalonBot.players
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.ping

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