package avalon.commands.game

import avalon.game.Avalon
import common.commands.MessageCommand
import common.commands.State
import common.game.Game
import common.game.GameType
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Color
import lib.model.channel.Message
import lib.util.ping
import lib.util.underline

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
object InfoCommand : MessageCommand(State.Avalon.Game) {
    override val name: String = "info"

    override val description: String = """
        Displays various info about the current Avalon game, such as
        turn order, number of victories for each team, the current leader, and the round number
    """.trimIndent()

    override val usage: String = "info (only works while a game of Avalon is in progress)"

    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val state = (Game[message.channel(), GameType.Avalon] as Avalon).state
        message.reply {
            title = "Avalon Info".underline()
            color = when {
                state.goodWins > state.evilWins -> Color.blue
                state.goodWins > state.evilWins -> Color.gold
                else -> Color.red
            }

//          description = "Here's where there would be an edited pic of the Avalon board if I was cool"
            description = "Order of leaders\n".underline() + state.players.joinToString(separator = "\n") { it.name }
            addField("Number of Good Victories".underline(), "${state.goodWins}", true)
            addField("Number of Evil Victories".underline(), "${state.evilWins}", true)
            addField("Current Leader".underline(), state.leader.user.ping(), true)
            addField("Round Number".underline(), "${state.roundNum}", true)
        }
    }
}
