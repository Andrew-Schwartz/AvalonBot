package avalonBot.commands

import avalonBot.characters.Character.Loyalty.Evil
import avalonBot.characters.Character.Loyalty.Good
import avalonBot.players
import avalonBot.roles
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message

object StartCommand : Command() {
    private const val START_NOW = "now"

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
            else -> null
        }

        val (good, evil) = roles.partition { it.loyalty == Good }.run { first.size to second.size }

        when {
            maxEvil == null -> message.reply("Between 5 and 10 players are required!")
            roles.size > players.size -> message.reply("You have more roles chosen than you have players!")
            evil > maxEvil -> {
                message.reply("You have too many evil roles (${roles.filter { it.loyalty == Evil }.joinToString { it.name }})")
            }
            evil == maxEvil -> {
                if (args.isNotEmpty()) {
                    if (args[0] == START_NOW) {
                        // TODO game of avalon
                    }
                }
            }
        }
    }
}