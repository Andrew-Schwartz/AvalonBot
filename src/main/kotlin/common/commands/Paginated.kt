package common.commands

import common.util.L
import common.util.elapsed
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import lib.dsl.*
import lib.model.ChannelId
import lib.model.emoji.asChar
import lib.rest.http.httpRequests.deleteAllReactions
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import java.time.Duration
import java.time.Instant

abstract class Paginated(val channel: ChannelId, val pages: List<RichEmbed>) {
    var idx = 0

    @ExperimentalCoroutinesApi
    @KtorExperimentalAPI
    suspend fun send(timeout: Duration = Duration.ofSeconds(30)) {
        val reply = channel.channel().send(embed = pages[0])
        var start: Instant = Instant.now()
        val bck = '◀'
        val fwd = '▶'
        Bot.launch {
            reply.react(bck)
            reply.react(fwd)
        }
        val scroll = object : ReactCommand(State.Arbitrary(this@Paginated)) {
            override val emojis: List<String> = L[fwd.toString(), bck.toString()]

            override val execute: suspend (MessageReactionUpdatePayload) -> Unit = { reaction ->
                if (reaction.messageId == reply.id) { // prevent 2 messages in same channel from linking
//                    Bot.launch {
//                        deleteReaction(reaction.channelId, reaction.messageId, reaction.emoji.asChar, reaction.userId)
//                    }
                    val delta = when (reaction.emoji.asChar) {
                        fwd -> 1
                        bck -> -1
                        else -> 0
                    }
                    start = Instant.now()
                    idx += delta
                    idx = when {
                        idx < 0 -> pages.size + idx
                        idx >= pages.size -> pages.size - idx
                        else -> idx
                    }
                    reply.edit(embed = pages[idx])
                }
            }
        }
        Bot.launch {
            ReactCommand.addCommand(scroll, channel)
            suspendUntil(1000) { start.elapsed() >= timeout }
            ReactCommand.removeCommand(scroll, channel)
            deleteAllReactions(reply.channelId, reply)
        }
    }
}
