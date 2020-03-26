package avalon.commands.setup

import avalon.characters.Character.Loyalty.Evil
import avalon.characters.Character.Loyalty.Good
import avalon.game.Avalon
import avalon.game.AvalonConfig
import common.commands.Command
import common.commands.CommandState
import common.commands.CommandState.AvalonGame
import common.game.Game
import common.game.GameType
import common.game.Setup
import common.steadfast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.Bot
import lib.dsl.blockUntil
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin

object StartAvalonCommand : Command(CommandState.Setup, AvalonGame) {
    private const val START_NOW = "now"
    private const val START_OVER = "over"

    override val name: String = "start"

    override val description: String = "if all players are ready, the game will start"

    override val usage: String = """!start ["$START_NOW"]"""

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    private suspend fun Bot.startGame(message: Message) {
        val setup = Setup[message.channel, GameType.Avalon]
        val roles = (setup.config as AvalonConfig).roles
        val maxEvil = when (setup.players.size) {
            in 5..6 -> 2
            in 7..9 -> 3
            10 -> 4
            else -> -1
        }

        val (_, evil) = roles.partition { it.loyalty == Good }.run { first.size to second.size }

        when {
            maxEvil == -1 -> message.reply("Between 5 and 10 players are required!")
            roles.size > setup.players.size -> message.reply("You have more roles chosen than you have players!")
            evil > maxEvil -> {
                message.reply("You have too many evil roles (${roles.filter { it.loyalty == Evil }.joinToString { it.name }})")
            }
            evil <= maxEvil -> {
                message.channel.commandState = AvalonGame
                GlobalScope.launch {
                    val avalon = Game[message.channel, GameType.Avalon] as Avalon
                    avalon.state.numEvil = maxEvil
                    avalon.state.ladyEnabled = LadyToggleCommand.enabled[message.channel] ?: false
                    Game.startGame(avalon)
//                    avalon.startGame()
                }
            }
            else -> message.reply("error starting game!!!")
        }
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val setup = Setup[message.channel, GameType.Avalon]
        val roles = (setup.config as AvalonConfig).roles
        when (args.getOrNull(0)) {
            START_OVER -> {
                Game.remove(message.channel, GameType.Avalon)
                message.channel.commandState = CommandState.Setup
                setup.players.clear()
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
                            setup.players.all { it.user in approves } -> true
                            setup.players.any { it.user in rejects } -> false
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