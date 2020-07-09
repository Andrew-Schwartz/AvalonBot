package avalon.commands.game

import avalon.game.AvalonState
import common.commands.ReactCommand
import common.commands.State
import common.game.Game
import common.util.A
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Add
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Remove

object VoteCommand : ReactCommand(State.Avalon.Voting) {
    const val approveChar = '✔'
    const val rejectChar = '❌'

    override val emojis: List<String> = A[approveChar, rejectChar].map(Char::toString)

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (MessageReactionUpdatePayload) -> Unit = { reaction ->
        Game.forUser(reaction.userId).forEach { game ->
            val state = AvalonState.inChannel(game.channel)
            val message = reaction.message()

            if (reaction.user().isBot != true && message in state?.reacts?.keys ?: emptySet<Int>()) {
                val delta = when (reaction.emoji.name[0]) {
                    approveChar -> 1
                    rejectChar -> -1
                    else -> 0
                } * when (reaction.type) {
                    Add -> 1
                    Remove -> -1
                }
                state?.reacts?.set(message, state.reacts[message]!! + delta)
            }
        }
    }
}