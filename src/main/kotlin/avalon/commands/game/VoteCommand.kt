package avalon.commands.game

import avalon.game.Avalon
import common.commands.ReactCommand
import common.commands.State
import common.game.Game
import common.game.GameType
import common.util.A
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Add
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Remove

object VoteCommand : ReactCommand(State.Avalon.Voting) {
    const val approveChar = '✔'
    const val rejectChar = '❌'

    override val emojis: Array<String> = A[approveChar.toString(), rejectChar.toString()]

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(MessageReactionUpdatePayload) -> Unit = { reaction ->
        val state = (Game[reaction.channel(), GameType.Avalon] as Avalon).state
        val message = reaction.message()

        if (reaction.user().isBot != true && message in state.reacts.keys) {
            val delta = when (reaction.emoji.name[0]) {
                approveChar -> 1
                rejectChar -> -1
                else -> 0
            } * when (reaction.type) {
                Add -> 1
                Remove -> -1
            }
            state.reacts[message] = state.reacts[message]!! + delta
        }
    }
}