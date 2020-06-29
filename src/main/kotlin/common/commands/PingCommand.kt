package common.commands

import common.commands.State.All
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.embed
import lib.model.Color
import lib.model.channel.Message
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

object PingCommand : MessageCommand(All) {
    override val name: String = "ping"

    override val description: String = "pongs, and says how long it took"

    override val usage: String = "ping"

    @ExperimentalTime
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val embed = embed {
            title = "Pong!"
            color = Color.gold
        }
        val (msg, time) = measureTimedValue {
            message.reply(embed = embed)
        }
        msg.edit(embed = embed) {
            footerText = "Took ${time.inSeconds} seconds to respond"
        }
    }
}