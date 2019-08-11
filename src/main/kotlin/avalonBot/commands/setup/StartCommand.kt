package avalonBot.commands.setup

import avalonBot.characters.Character.Loyalty.Evil
import avalonBot.characters.Character.Loyalty.Good
import avalonBot.commands.Command
import avalonBot.commands.CommandState.AvalonGame
import avalonBot.commands.CommandState.Setup
import avalonBot.commands.currentState
import avalonBot.game.Avalon
import avalonBot.players
import avalonBot.roles
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.Bot
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin

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
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val maxEvil = when (players.size) {
            in 5..6 -> 2
            in 7..9 -> 3
            10 -> 4
            else -> -1
        }

        val (good, evil) = roles.partition { it.loyalty == Good }.run { first.size to second.size }

        when {
            args.getOrNull(0) == START_OVER -> {
                avalonInstance = null
                currentState = Setup
                players.clear()
                roles.clear()
                for (pin in pinnedMessages) {
                    runCatching { deletePin(pin.channelId, pin.id) }
                            .onFailure { println(it.message) }
                }
            }
//            args.getOrNull(0) == START_NOW -> {
//                if (message.author != steadfast) {
//                    message.reply("Only Andrew is that cool")
//                } else {
//
//                }
//            }
            maxEvil == -1 -> message.reply("Between 5 and 10 players are required!")
            roles.size > players.size -> message.reply("You have more roles chosen than you have players!")
            evil > maxEvil -> {
                message.reply("You have too many evil roles (${roles.filter { it.loyalty == Evil }.joinToString { it.name }})")
            }
            evil <= maxEvil -> {
                currentState = AvalonGame
                val bot = this
                GlobalScope.launch {
                    avalonInstance = Avalon(bot, message.channel)
                    avalonInstance!!.startGame(numEvil = maxEvil, ladyEnabled = LadyCommand.enabled)
                }
            }
            else -> message.reply("error starting game!!!")
        }
    }
}