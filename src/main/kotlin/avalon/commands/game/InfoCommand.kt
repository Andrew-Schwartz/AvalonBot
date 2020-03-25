package avalon.commands.game

import avalon.Colors
import avalon.game.Avalon
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.ping
import lib.util.underline
import main.commands.Command
import main.commands.CommandState.AvalonGame

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class InfoCommand(private val state: Avalon) : Command(AvalonGame) {
    override val name: String = "info"

    override val description: String = """
        Displays various info about the current Avalon game, such as
        turn order, number of victories for each team, the current leader, and the round number
    """.trimIndent()

    override val usage: String = "!info (only works while a game of Avalon is in progress)"

    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        message.reply {
            title = "Avalon Info".underline()
            color = when {
                state.goodWins > state.evilWins -> Colors.good
                state.goodWins > state.evilWins -> Colors.neutral
                else -> Colors.evil
            }

//          description = "Here's where there would be an edited pic of the Avalon board if I was cool"
            description = "Order of leaders\n".underline() + state.gamePlayers.joinToString(separator = "\n") { it.nameAndUser }
            addField("Number of Good Victories".underline(), "$state.goodWins", true)
            addField("Number of Evil Victories".underline(), "$state.evilWins", true)
            addField("Current Leader".underline(), state.gamePlayers[state.rounds[state.roundNum].players].user.ping(), true)
            addField("Round Number".underline(), "$state.roundNum", true)
//          addField("Number of rejected party proposals", rounds[roundNum].fails.toString(), true) that's how many is required not happened lol
        }
    }
}
