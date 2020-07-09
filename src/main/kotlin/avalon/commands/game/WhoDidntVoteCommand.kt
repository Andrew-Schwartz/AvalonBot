package avalon.commands.game

import avalon.game.Avalon
import common.commands.MessageCommand
import common.commands.State
import common.game.Game
import common.game.GameType
import common.util.listGrammatically
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.reply
import lib.model.channel.Message

object WhoDidntVoteCommand : MessageCommand(State.Avalon.Voting) {
    override val name: String = "whodidntvote"

    override val description: String = "find out who didn't vote"

    override val usage: String = "whodidntvote"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val state = (Game[message.channel(), GameType.Avalon] as Avalon).state
        val notVoted = state.players.filter {
            it.user in state.reacts
                    .filterValues { it == 0 }
                    .map { (msg, _) -> msg.channel().recipients?.singleOrNull() }
        }.map { it.name }
        message.reply("${notVoted.listGrammatically("no one")} ${if (notVoted.size < 2) "has" else "have"} not voted")
    }
}