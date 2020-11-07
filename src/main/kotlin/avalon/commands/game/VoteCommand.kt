package avalon.commands.game

import common.commands.ReactCommand
import common.commands.State
import common.util.A
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Message
import lib.model.emoji.asChar
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Add
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Remove

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class VoteCommand(votes: MutableMap<Message, Int>) : ReactCommand(State.Avalon.Voting) {
    override val emojis: List<String> = A[approveChar, rejectChar].map(Char::toString)

    override val execute: suspend (MessageReactionUpdatePayload) -> Unit = { reaction ->
        val message = reaction.message()

        if (reaction.user().isBot != true && message in votes.keys) {
            val delta = when (reaction.emoji.asChar) {
                approveChar -> 1
                rejectChar -> -1
                else -> 0
            } * when (reaction.type) {
                Add -> 1
                Remove -> -1
            }
            votes[message] = votes[message]!! + delta
        }
    }

    companion object {
        const val approveChar = '✅'
        const val rejectChar = '❌'
    }
}
