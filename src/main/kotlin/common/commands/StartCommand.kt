package common.commands

import common.game.Game
import common.game.GameType
import common.game.Setup
import common.steadfast
import common.util.getOrDefault
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.Bot
import lib.dsl.blockUntil
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin

object StartCommand : Command(CommandState.Setup) {
    private const val START_NOW = "now"
    private const val START_OVER = "over"

    override val name: String = "start"

    override val description: String = """
        Starts the game if all players are ready. You must specify which game to start (Avalon or Exploding Kittens)
    """.trimIndent()

    override val usage: String = """!start <game> ["$START_NOW"]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        with(message) {
            // exists for the return below to work
            val gameFromSetups = GameType.values()
                    .map { it to Setup[message.channel, it] }
                    .singleOrNull { it.second.players.isNotEmpty() }
                    ?.first
            val gameFromArgs = GameType.getType(args.getOrDefault(0, ""))

            val gameType = gameFromArgs
                    ?: gameFromSetups
                    ?: message.reply("Specify which game to start", ping = true).let { return@with }

            val setup = Setup[message.channel, gameType]
            when (args.lastOrNull()) {
                START_OVER -> {
                    Game.endAndRemove(message.channel, gameType)
                    message.channel.commandState = CommandState.Setup
                    Setup.remove(message.channel, gameType)
                    for (pin in pinnedMessages) {
                        runCatching { deletePin(pin.channelId, pin.id) }
                                .onFailure { println(it.message) }
                    }
                }
                START_NOW -> {
                    if (message.author != steadfast) {
                        message.reply("Only Andrew is that cool")
                    } else {
                        gameType.startGame(message)
                    }
                }
                else -> {
                    val approveChar = '✔'
                    val rejectChar = '❌'
                    val botMsg = message.reply("React ✔ if you are ready to start the game, if you're not ready react ❌")
                    botMsg.react(approveChar)
                    botMsg.react(rejectChar)

                    GlobalScope.launch {
                        blockUntil {
                            val (approves, rejects) = botMsg.reactions(approveChar, rejectChar)
                            when {
                                setup.players.all { it.user in approves } -> true
                                setup.players.any { it.user in rejects } -> false
                                rejects.size >= 3 -> false
                                else -> false
                            }
                        }

                        gameType.startGame(message)
                    }
                }
            }
        }
    }
}