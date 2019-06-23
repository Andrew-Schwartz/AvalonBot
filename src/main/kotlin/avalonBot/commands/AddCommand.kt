package avalonBot.commands

import avalonBot.neutral
import avalonBot.players
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message
import lib.util.inlineCode
import lib.util.underline

object AddCommand : Command {
    override val name: String
        get() = "addme"

    override val description: String
        get() = "adds player who sent this to game of Avalon"

    override val usage: String
        get() = "!addme [nickname]".inlineCode()

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit
        get() = { message, args ->
            val nick = if (args.isNotEmpty()) args[0] else message.author.username

            // TODO commented for testing alone
//            if (message.author in players.values) {
//                message.reply(ping = true, content = ", you are already in the game!")
//            } else {
            players[nick] = message.author
            message.reply {
                color = neutral
                addField("Current Players".underline(), players.keys.joinToString(separator = "\n"))
            }
//            }
        }
}