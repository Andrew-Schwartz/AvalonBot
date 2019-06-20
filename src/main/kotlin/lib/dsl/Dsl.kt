package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.Guild
import lib.model.Message
import lib.rest.model.events.receiveEvents.ReadyEvent
import lib.util.Action

val readyEvents: ArrayList<Action<ReadyEvent>> = arrayListOf()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun Bot.onReady(λ: suspend ReadyEvent.() -> Unit) {
    readyEvents += λ
}

val messageCreateEvents: ArrayList<Action<Message>> = arrayListOf()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun Bot.onMessageCreate(prefix: String = "", triggerOnBots: Boolean = false, λ: suspend Message.() -> Unit) {
    messageCreateEvents += {
        if (content.startsWith(prefix) && (triggerOnBots || author.bot != true))
            λ()
    }
}

val guildCreateEvents: ArrayList<Action<Guild>> = arrayListOf()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun Bot.onGuildCreate(λ: suspend Guild.() -> Unit) {
    guildCreateEvents += λ
}
