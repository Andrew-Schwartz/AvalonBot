package avalon.commands.game

import avalon.game.Avalon
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import main.commands.Command
import main.commands.CommandState.AvalonGame
import main.util.onNull

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class QuestCommand(private val state: Avalon) : Command(AvalonGame) {
    override val name: String = "quest"

    override val description: String = "Choose which people will go on the quest! Only usable by the current leader"

    override val usage: String = "!quest <player1>;<player2>;...  (the semicolons are necessary)"

    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        with(message) {
            if (author != state.leader.user) return@with
            val round = state.rounds[state.roundNum]

            val questers = content.substring(content.indexOf(' ') + 1)
                    .split(" *; *".toRegex())
                    .mapNotNull { arg ->
                        state.playerByName(arg).onNull { reply("No one by the (nick)name $arg") }
                    }
                    .toSet()

            if (questers.size != round.players) {
                reply(ping = true, content = "\nYou need to send ${round.players} people on the quest!")
                return@with
            }
            state.party = questers
        }
    }
}
