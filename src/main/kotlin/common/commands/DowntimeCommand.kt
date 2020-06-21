package common.commands

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Color
import lib.model.channel.Message
import java.time.OffsetDateTime

object DowntimeCommand : MessageCommand(State.All) {
    override val name: String = "downtime"

    override val description: String = "How long has this bot been offline for?"

    override val usage: String = "downtime"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        message.reply {
            title = "Approximately 13.7 billion years"
            timestamp(OffsetDateTime.MIN)
            color = Color.gold
        }
    }
}