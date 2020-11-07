package common.commands

import common.commands.StopCommand.approveChar
import common.commands.StopCommand.rejectChar
import common.game.Game
import common.game.GameFinish
import common.game.GameType
import common.steadfast
import common.util.A
import common.util.Vote
import common.util.debug
import common.util.getOrDefault
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import lib.dsl.*
import lib.model.Color.Companion.gold
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.emoji.asChar
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload

object StopCommand : MessageCommand(State.Game) {
    const val approveChar = '✅'
    const val rejectChar = '❌'

    override val name: String = "restart"

    override val description: String = "Restart a game if all players agree. You must specify which game to start (Avalon or Exploding Kittens)"

    override val usage: String = "restart <game> ['now']"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        with(message) {
            val gameFromGames = GameType.values()
                    .map { it to Game[message.channel(), it] }
                    .singleOrNull { it.second?.running == true }
                    ?.first
            val gameFromArgs = GameType.getType(args.getOrDefault(0, ""))

            val gameType = gameFromArgs
                    ?: gameFromGames
                    ?: message.reply(" Specify which game to restart", ping = true).let { return@with }

            val game = Game[message.channel(), gameType]!!
            if (args.lastOrNull()?.equals("now", true) == true) {
                if (author != steadfast) {
                    message.reply("Only Andrew is that cool")
                } else {
                    Game.endAndRemove(message.channel(), gameType, GameFinish(embed {
                        title = "Manually restarted"
                        color = gold
                    }))
                }
            } else {
                val botMsg = message.reply("React ✅ if you agree to restart the game, if not react ❌")
                botMsg.react(approveChar)
                botMsg.react(rejectChar)
                RestartVoteCommand.restarts[message.channel()] = Vote(botMsg)
                Bot.launch {
                    var cancelled = false
                    suspendUntil(500) {
                        if (State.Game !in channel().states) {
                            cancelled = true
                            return@suspendUntil true
                        }
                        val score = RestartVoteCommand.restarts[message.channel()]?.score ?: return@suspendUntil false
                        if (!channelId.debug && score != game.state.players.size) return@suspendUntil false
                        val (approves, rejects) = botMsg.reactions(approveChar, rejectChar)
                        game.state.players.none { it.user in rejects } &&
                                game.state.players.count { it.user in approves } >= 4
                    }
                    if (!cancelled) {
                        Game.endAndRemove(message.channel(), gameType, GameFinish(embed {
                            title = "Manually restarted"
                            color = gold
                        }))
                    }
                }
            }
        }
    }
}

object RestartVoteCommand : ReactCommand(State.Game) {
    internal val restarts = mutableMapOf<Channel, Vote>()

    override val emojis: List<String> = A[approveChar, rejectChar].map(Char::toString)

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (MessageReactionUpdatePayload) -> Unit = { reaction ->
        val vote = restarts[reaction.channel()]
        if (reaction.message() == vote?.message) {
            val delta = when (reaction.emoji.asChar) {
                StartCommand.approveChar -> 1
                StartCommand.rejectChar -> -1
                else -> 0
            } * when (reaction.type) {
                MessageReactionUpdatePayload.Type.Add -> 1
                MessageReactionUpdatePayload.Type.Remove -> -1
            }
            vote.score += delta
        }
    }
}