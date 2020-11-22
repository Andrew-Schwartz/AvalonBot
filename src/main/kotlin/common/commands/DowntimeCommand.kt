package common.commands

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.channel
import lib.dsl.send
import lib.model.Color
import lib.model.channel.Message

object DowntimeCommand : MessageCommand(State.All) {
    override val name: String = "downtime"

    override val description: String = "How long has this bot been offline for?"

    override val usage: String = "downtime"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        message.channel().send {
            title = "Approximately 13.8 billion years"
            Bot.logInTime?.let { timestamp(it) }
            color = Color.gold
        }
    }
}