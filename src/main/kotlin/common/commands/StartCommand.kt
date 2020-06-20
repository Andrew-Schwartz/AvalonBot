package common.commands

import common.commands.StartCommand.approveChar
import common.commands.StartCommand.rejectChar
import common.game.GameType
import common.game.Setup
import common.steadfast
import common.util.A
import common.util.Vote
import common.util.debug
import common.util.getOrDefault
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.Bot
import lib.dsl.suspendUntil
import lib.model.channel.Message
import lib.rest.http.httpRequests.getMessage
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Add
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Remove

object StartCommand : MessageCommand(State.Setup.Setup) {
    const val approveChar = '✔'
    const val rejectChar = '❌'

    override val name: String = "start"

    override val description: String = """
        Starts the game if all players are ready. You must specify which game to start (Avalon or Exploding Kittens).
        Optionally specify "now" to start the game immediately even if some people aren't ready
    """.trimIndent()

    override val usage: String = """start <game> ["now"]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        // exists for the return below to work
        with(message) {
            val gameFromSetups = GameType.values()
                    .map { it to Setup[message.channel(), it] }
                    .singleOrNull { it.second.players.isNotEmpty() }
                    ?.first
            val gameFromArgs = GameType.getType(args.getOrDefault(0, ""))

            val gameType = gameFromArgs
                    ?: gameFromSetups
                    ?: message.reply(" Specify which game to start", ping = true).let { return@with }

            val setup = Setup[message.channel(), gameType]

            if (args.lastOrNull() == "now") {
                if (message.author != steadfast) {
                    message.reply("Only Andrew is that cool")
                } else {
                    channel().states -= GameType.values().map { it.states.startVotingState }
                    gameType.startGame(message)
                }
            } else {
                if (State.Setup.AvalonStart in channel().states || State.Setup.KittensStart in channel().states) {
                    val link = setup.startVote?.message?.let { msg ->
                        val msg = getMessage(msg.channelId, msg, forceRequest = true)
                        if (msg.guildId == null) { // DM
                            "https://discordapp.com/channels/@me/${msg.channelId}/${msg.id}"
                        } else {
                            "https://discordapp.com/channels/${msg.guildId}/${msg.channelId}/${msg.id}"
                        }
                    } ?: ""
                    message.reply(" You are already voting to start the game here!\n$link", ping = true)
                    return@with
                }
                val botMsg = message.reply("React ✔ if you are ready to start the game, if you're not ready react ❌")
                botMsg.react(approveChar)
                botMsg.react(rejectChar)
                setup.startVote = Vote(botMsg)
                val votingState = gameType.states.startVotingState
                channel().states += votingState

                GlobalScope.launch {
                    var cancelled = false
                    suspendUntil(500) {
                        if (votingState !in channel().states) {
                            cancelled = true
                            return@suspendUntil true
                        }
                        val score = setup.startVote?.score ?: return@suspendUntil false
                        if (!channelId.debug && score != setup.players.size) return@suspendUntil false
                        val (approves, rejects) = botMsg.reactions(approveChar, rejectChar)
                        when {
                            setup.players.all { it.user in approves } -> true
                            setup.players.any { it.user in rejects } -> false
                            rejects.size >= 3 -> false
                            else -> false
                        }
                    }
                    if (!cancelled) {
                        channel().states -= votingState
                        gameType.startGame(message)
                    }
                }
            }
        }
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun voteExecute(gameType: GameType): suspend Bot.(MessageReactionUpdatePayload) -> Unit = { reaction ->
    val setup = Setup[reaction.channel(), gameType]

    if (reaction.message() == setup.startVote?.message) {
        val delta = when (reaction.emoji.name[0]) {
            approveChar -> 1
            rejectChar -> -1
            else -> 0
        } * when (reaction.type) {
            Add -> 1
            Remove -> -1
        }
        setup.startVote?.score?.inc()
    }
}

object AvalonVoteCommand : ReactCommand(State.Setup.AvalonStart) {
    override val emojis: List<String> = A[approveChar, rejectChar].map(Char::toString)

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(MessageReactionUpdatePayload) -> Unit = voteExecute(GameType.Avalon)
}

object KittensVoteCommand : ReactCommand(State.Setup.KittensStart) {
    override val emojis: List<String> = A[approveChar, rejectChar].map(Char::toString)

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(MessageReactionUpdatePayload) -> Unit = voteExecute(GameType.Kittens)
}
