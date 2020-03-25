package main.commands

import avalon.Colors
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.embed
import lib.model.channel.Message
import main.commands.CommandState.All
import kotlin.system.measureTimeMillis

object PingCommand : Command(All) {
    override val name: String = "ping"

    override val description: String = "pongs, and says how long it took"

    override val usage: String = "!ping"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        val embed = embed {
            title = "Pong!"
            color = Colors.neutral
        }
        var msg: Message? = null
        val time = measureTimeMillis {
            msg = message.reply(embed = embed)
        }
        msg!!.edit(embed = embed) {
            footerText = "Took $time ms to respond"
        }
    }
}