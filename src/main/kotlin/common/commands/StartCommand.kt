package common.commands

import common.game.GameType
import common.game.Setup
import common.steadfast
import common.util.getOrDefault
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.Bot
import lib.dsl.suspendUntil
import lib.model.channel.Message

object StartCommand : Command(State.Setup) {
    private const val START_NOW = "now"

    override val name: String = "start"

    override val description: String = """
        Starts the game if all players are ready. You must specify which game to start (Avalon or Exploding Kittens)
    """.trimIndent()

    override val usage: String = """start <game> ["$START_NOW"]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
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
            if (args.lastOrNull() == START_NOW) {
                if (message.author != steadfast) {
                    message.reply("Only Andrew is that cool")
                } else {
                    gameType.startGame(message)
                }
            } else {
                val approveChar = '✔'
                val rejectChar = '❌'
                val botMsg = message.reply("React ✔ if you are ready to start the game, if you're not ready react ❌")
                botMsg.react(approveChar)
                botMsg.react(rejectChar)

                GlobalScope.launch {
                    suspendUntil {
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