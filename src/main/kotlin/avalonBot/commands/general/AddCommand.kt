package avalonBot.commands.general

import avalonBot.commands.Command
import avalonBot.commands.CommandState.General
import avalonBot.neutral
import avalonBot.players
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message
import lib.util.inlineCode
import lib.util.underline

object AddCommand : Command(General) {
    override val name: String = "addme"

    override val description: String = "adds player who sent this to game of Avalon"

    override val usage: String = "!addme [nickname]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val nick = if (args.isNotEmpty()) args[0] else message.author.username

        when {
            // TODO commented for testing alone
//            message.author in players.values -> message.reply(ping = true, content = ", you are already in the game!")
//            message.channel.guild?.members?.any { it.username == nick || it.nick == nick } != false -> message.reply(ping = true, content = ", a member of this server has that name. That would be confusing :(")
            nick.contains(";") -> message.reply(ping = true, content = ", names cannot contain ${";".inlineCode()}")
            nick.contains("\n") -> message.reply(ping = true, content = ", names cannot contain newlines")
            else -> {
                players[nick] = message.author
                message.reply {
                    color = neutral
                    addField("Current Players".underline(), players.keys.joinToString(separator = "\n"))
                }
            }
        }
    }
}