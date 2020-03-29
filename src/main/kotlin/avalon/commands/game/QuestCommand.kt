package avalon.commands.game

import avalon.game.Avalon
import common.commands.Command
import common.commands.CommandState.AvalonGame
import common.game.Game
import common.game.GameType
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
object QuestCommand : Command(AvalonGame) {
    override val name: String = "quest"

    override val description: String = "Choose which people will go on the quest! Only usable by the current leader"

    override val usage: String = "!quest <@player1> <@player2>..."

    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        val state = (Game[message.channel, GameType.Avalon] as Avalon).state

        with(message) {
            if (author != state.leader.user) return@with
            val round = state.rounds[state.roundNum]

            val questers = mentions
                    .mapNotNull { state.userPlayerMap[it] }
//                    .filterNot { it.user == author }
                    .toSet()

            if (questers.size != round.players) {
                reply(ping = true, content = "\nYou need to send ${round.players} people on the quest!")
                return@with
            }
            state.party = questers
        }
    }
}
