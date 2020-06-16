package common.commands

import common.commands.State.All
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.embed
import lib.model.Color
import lib.model.channel.Message
import kotlin.system.measureTimeMillis

object PingCommand : MessageCommand(All) {
    override val name: String = "ping"

    override val description: String = "pongs, and says how long it took"

    override val usage: String = "ping"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val embed = embed {
            title = "Pong!"
            color = Color.gold
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