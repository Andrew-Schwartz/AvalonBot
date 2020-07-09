package common.commands

import common.util.elapsed
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.reply
import lib.model.Color.Companion.gold
import lib.model.channel.Message
import java.time.Duration

object UptimeCommand : MessageCommand(State.All) {
    override val name: String = "uptime"

    override val description: String = "How long has this bot been continuously online for?"

    override val usage: String = "uptime"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        message.reply {
            title = formatDuration(Bot.logInTime!!.toInstant().elapsed())
            if (Bot.logInTime != Bot.firstLogInTime) {
                addField("Time since initial login", formatDuration(Bot.firstLogInTime!!.toInstant().elapsed()))
            }
            timestamp(Bot.logInTime!!)
            color = gold
        }
    }
}

private fun formatDuration(duration: Duration) = buildString {
    var dur = duration
    val days = dur.toDays()
    if (days > 0) {
        if (days == 1L) append("1 day, ") else append("$days days, ")
        dur = dur.minusDays(days)
    }
    val hours = dur.toHours()
    if (hours > 0) {
        if (hours == 1L) append("1 hour, ") else append("$hours hours, ")
        dur = dur.minusHours(hours)
    }
    val mins = dur.toMinutes()
    if (mins > 0) {
        if (mins == 1L) append("1 min, ") else append("$mins mins, ")
        dur = dur.minusMinutes(mins)
    }
    val secs = dur.toSeconds()
    dur = dur.minusSeconds(secs)
    val millis = dur.toMillis()
    append("$secs.$millis secs")
}