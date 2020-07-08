package avalon.commands.game

import avalon.game.AvalonState
import common.commands.MessageCommand
import common.commands.State
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
object QuestCommand : MessageCommand(State.Avalon.Quest) {
    override val name: String = "quest"

    override val description: String = "Choose which people will go on the quest! Only usable by the current leader"

    override val usage: String = "quest <@player1> <@player2>..."

    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val state = AvalonState.inChannel(message.channel())

        with(message) {
            if (author != state?.leader?.user) return@with
            val round = state.rounds[state.roundNum]

            val questers = mentions
                    .mapNotNull { state.userPlayerMap[it] }
                    .toSet()

            if (questers.size != round.players) {
                reply(ping = true, content = "\nYou need to send ${round.players} people on the quest!")
                return@with
            }
            state.party = questers
        }
    }
}
