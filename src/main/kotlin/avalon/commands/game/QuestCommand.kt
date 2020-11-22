package avalon.commands.game

import avalon.game.AvalonPlayer
import avalon.game.AvalonState
import common.commands.MessageCommand
import common.commands.State
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.reply
import lib.model.channel.Message

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class QuestCommand(state: AvalonState, setter: (Set<AvalonPlayer>) -> Unit) : MessageCommand(State.Avalon.Quest) {
    override val name: String = "quest"

    override val description: String = "Choose which people will go on the quest! Only usable by the current leader"

    override val usage: String = "quest <@player1> <@player2>..."

    override val execute: suspend (Message) -> Unit = { message ->
        with(state) {
            if (message.author != leader.user) return@with
            val round = rounds[roundNum]

            val questers = message.mentions
                    .mapNotNull { userPlayerMap[it] }
                    .toSet()

            if (questers.size != round.players) {
                message.reply("You need to send ${round.players} people on the quest!", ping = true)
                return@with
            }
            setter(questers)
        }
    }
}
