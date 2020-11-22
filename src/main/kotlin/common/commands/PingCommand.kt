package common.commands

import common.commands.State.All
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.edit
import lib.dsl.embed
import lib.dsl.send
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
    override val execute: suspend (Message) -> Unit = { message ->
        val embed = embed {
            title = "Pong!"
            color = Color.gold
        }
        val (msg, time) = measureTimedValue {
            message.channel().send(embed = embed)
        }
        msg.edit(embed = embed) {
            footerText = "Took ${time.inSeconds} seconds to respond"
        }
    }
}