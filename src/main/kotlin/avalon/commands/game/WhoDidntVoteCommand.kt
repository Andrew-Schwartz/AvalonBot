package avalon.commands.game

import avalon.game.Avalon
import common.commands.Command
import common.commands.State
import common.game.Game
import common.game.GameType
import common.util.listGrammatically
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

// TODO fix
object WhoDidntVoteCommand : Command(State.Avalon.Voting) {
    override val name: String = "whodidntvote"

    override val description: String = "find out who didn't vote"

    override val usage: String = "whodidntvote"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        val avalon = Game[message.channel(), GameType.Avalon] as Avalon
        val notVoted = avalon.state.players.filter {
            it.user in avalon.state.reacts
                    .filterValues { it == 0 }
                    .map { (msg, _) -> msg.channel().recipients?.singleOrNull() }
        }.map { it.name }
        message.reply("${notVoted.listGrammatically()} have not voted")
    }
}