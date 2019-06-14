package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.misc.Action
import lib.model.Message
import lib.rest.model.events.receiveEvents.ReadyEvent

val readyEvents: ArrayList<Action<ReadyEvent>> = arrayListOf()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun BotBuilder.onReady(λ: suspend ReadyEvent.() -> Unit) {
    readyEvents += λ
}

val messageCreateEvents: ArrayList<Action<Message>> = arrayListOf()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
fun BotBuilder.onMessage(prefix: String, triggerOnBots: Boolean = false, λ: suspend Message.() -> Unit) {
    messageCreateEvents += {
        if (content.startsWith(prefix) && (triggerOnBots || author.bot != true))
            λ()
    }
}
