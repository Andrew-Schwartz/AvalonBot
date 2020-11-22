package avalon.commands.game

import avalon.game.AvalonState
import common.commands.MessageCommand
import common.commands.State
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.reply
import lib.model.Color
import lib.model.channel.Message
import lib.util.pingNick
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

    override val execute: suspend (Message) -> Unit = { message ->
        val state = AvalonState.inChannel(message.channel())
        state?.run {
            message.reply {
                title = "Avalon Info".underline()
                color = when {
                    state.goodWins > state.evilWins -> Color.blue
                    state.goodWins > state.evilWins -> Color.gold
                    else -> Color.red
                }

//          description = "Here's where there would be an edited pic of the Avalon board if I was cool"

                // todo did this work???
                val orderedLeaders = buildString {
                    append("Order of Leaders".underline())
                    append("\n")
                    val players = state.players
                    for (i in state.leaderNum until players.size) {
                        append(players[i].user.pingNick())
                        append("\n")
                    }
                    for (i in 0 until state.leaderNum) {
                        append(players[i].user.pingNick())
                        append("\n")
                    }
                }
                description = orderedLeaders
//                description = "Order of leaders\n".underline() + state.players.joinToString(separator = "\n") { it.name }
                addField("Number of Good Victories".underline(), "${state.goodWins}", true)
                addField("Number of Evil Victories".underline(), "${state.evilWins}", true)
                addField("Current Leader".underline(), state.leader.user.pingNick(), true)
                addField("Round Number".underline(), "${state.roundNum}", true)
            }
        }
    }
}
