package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.user.User
import lib.rest.http.httpRequests.createDM

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun User.getDM(): Channel = createDM(id)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun User.sendDM(
        content: String = "",
        embed: RichEmbed = RichEmbed(),
        builder: suspend RichEmbed.() -> Unit = {}
): Message {
    return getDM().send(
            content = content,
            embed = embed,
            builder = builder
    )
}