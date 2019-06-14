package lib.dsl

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.Bot
import lib.model.Channel
import lib.model.Message
import lib.model.User
import lib.rest.http.CreateDM
import lib.rest.http.CreateMessage
import lib.rest.http.createDM
import lib.rest.http.createMessage

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
//@DiscordDsl
class BotBuilder(token: String) {
    val bot = Bot(token)

    val user: User get() = bot.user

    fun build(): Bot {
        return bot
    }

    suspend fun Message.reply(content: String) {

    }

    suspend fun User.sendDM(content: String) {
        val channel = this@BotBuilder.bot.createDM(CreateDM(id.value))

        channel.sendMessage(content)
    }

    suspend fun Channel.sendMessage(content: String) {
        this@BotBuilder.bot.createMessage(this, CreateMessage(content = content))
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun bot(token: String, initBot: suspend BotBuilder.() -> Unit) {
    val bot = BotBuilder(token).apply { initBot() }.build()

    bot.socket.run()
}
