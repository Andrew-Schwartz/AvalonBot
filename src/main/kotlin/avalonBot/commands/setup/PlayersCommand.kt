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
import lib.util.inlineCode
import lib.util.ping

object PlayersCommand : Command(Setup, AvalonGame) {
    override val name: String = "players"

    override val description: String = "displays a list of all players currently in the game"

    override val usage: String = """!players ["ping"]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        if (players.isEmpty()) {
            message.reply("There are currently no players")
        } else {
            if (args.isNotEmpty()) {
                if (args[0].toLowerCase() == "ping") {
                    message.reply {
                        color = Colors.neutral
                        title = "Players"
                        addField("Nicknames", players.keys.joinToString(separator = "\n"))
                        addField("Users", players.values.joinToString(separator = "\n") { it.ping() })
                    }
                } else {
                    "${"!players ${args[0]}".inlineCode()} is not understood. Try ${"!help players".inlineCode()} for information"
                }
            } else {
                message.reply {
                    color = Colors.neutral
                    addField("Nicknames", players.keys.joinToString(separator = "\n"))
                    addField("Users", players.values.joinToString(separator = "\n") { it.username })
                }
            }
        }
    }
}