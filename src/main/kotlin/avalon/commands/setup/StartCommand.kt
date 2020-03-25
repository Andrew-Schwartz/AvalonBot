package avalon.commands.setup

import avalon.characters.Character.Loyalty.Evil
import avalon.characters.Character.Loyalty.Good
import avalon.game.Avalon
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.Bot
import lib.dsl.blockUntil
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin
import main.commands.Command
import main.commands.CommandState.AvalonGame
import main.commands.CommandState.Setup
import main.commands.currentState
import main.players
import main.roles
import main.steadfast

object StartCommand : Command(Setup, AvalonGame) {
    private const val START_NOW = "now"
    private const val START_OVER = "over"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    private var avalonInstance: Avalon? = null

//    @KtorExperimentalAPI
//    @ExperimentalCoroutinesApi
//    private val avalonGames: Map<Channel, Avalon> = emptyMap()

    override val name: String = "start"

    override val description: String = "if all players are ready, the game will start"

    override val usage: String = """!start ["$START_NOW"]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    private suspend fun Bot.startGame(message: Message) {
        val maxEvil = when (players.size) {
            in 5..6 -> 2
            in 7..9 -> 3
            10 -> 4
            else -> -1
        }

        val (good, evil) = roles.partition { it.loyalty == Good }.run { first.size to second.size }

        when {
            maxEvil == -1 -> message.reply("Between 5 and 10 players are required!")
            roles.size > players.size -> message.reply("You have more roles chosen than you have players!")
            evil > maxEvil -> {
                message.reply("You have too many evil roles (${roles.filter { it.loyalty == Evil }.joinToString { it.name }})")
            }
            evil <= maxEvil -> {
                currentState = AvalonGame
                GlobalScope.launch {
                    avalonInstance = Avalon(this@startGame, message.channel)
                    avalonInstance!!.numEvil = maxEvil
                    avalonInstance!!.ladyEnabled = LadyCommand.enabled
                    avalonInstance!!.startGame()
                }
            }
            else -> message.reply("error starting game!!!")
        }
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        when (args.getOrNull(0)) {
            START_OVER -> {
                avalonInstance = null
                currentState = Setup
                players.clear()
                roles.clear()
                for (pin in pinnedMessages) {
                    runCatching { deletePin(pin.channelId, pin.id) }
                            .onFailure { println(it.message) }
                }
            }
            START_NOW -> {
                if (message.author != steadfast) {
                    message.reply("Only Andrew is that cool")
                } else {
                    startGame(message)
                }
            }
            else -> {
                val approveChar = '✔'
                val rejectChar = '❌'
                val botMsg = message.reply("React ✔ if you are ready to start the game, if you're not ready react ❌")

                GlobalScope.launch {
                    blockUntil {
                        val (approves, rejects) = botMsg.reactions(approveChar, rejectChar)
                        when {
                            players.all { it.value in approves } -> true
                            players.any { it.value in rejects } -> false
                            rejects.size >= 3 -> false
                            else -> false
                        }
                    }

                    startGame(message)
                }
            }
        }
    }
}