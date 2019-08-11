package avalonBot.commands.setup

import avalonBot.Colors
import avalonBot.commands.Command
import avalonBot.commands.CommandState.Setup
import avalonBot.players
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import lib.util.inlineCode
import lib.util.underline

object AddCommand : Command(Setup) {
    override val name: String = "addme"

    override val description: String = "adds player who sent this to game of Avalon"

    override val usage: String = "!addme [nickname]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val nick: String = if (args.isNotEmpty()) message.content.substringAfter(" ") else message.member.nick
                ?: message.author.username

        when {
//            message.channel.guild?.members
//                    ?.filter { it.user != message.author }
//                    ?.any { it.username == nick || it.nick == nick } != false -> message.reply(ping = true, content = ", a member of this server has that name. That would be confusing :(")
            nick.contains(";") -> message.reply(ping = true, content = ", names cannot contain ${";".inlineCode()}")
            nick.contains("\n") -> message.reply(ping = true, content = ", names cannot contain newlines")
            nick in players -> message.reply(ping = true, content = ", $nick is already taken")
            else -> {
                if (message.author in players.values) {
                    val name = players.entries.firstOrNull { (_, v) -> v == message.author }?.key
                    players.remove(name)
                }
                players[nick] = message.author
                message.reply {
                    color = Colors.neutral
                    addField("Current Players".underline(), players.keys.joinToString(separator = "\n"))
                }
            }
        }
    }
}