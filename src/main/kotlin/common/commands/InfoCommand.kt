package common.commands

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.channel
import lib.dsl.send
import lib.model.Color
import lib.model.channel.Message

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
object InfoCommand : MessageCommand(State.All) {
    override val name: String = "info"

    override val description: String = "gets some useful information about this bot"

    override val usage: String = "info"

    override val execute: suspend (Message) -> Unit = { message ->
        message.channel().send {
            title = "Avalon Bot"
            url = "https://github.com/Andrew-Schwartz/AvalonBot"
            color = Color.gold

            val url = "https://discord.com/oauth2/authorize?scope=bot&client_id=${Bot.user.id}&permissions=${
                (Permissions.ADD_REACTIONS + Permissions.SEND_MESSAGES + Permissions.VIEW_CHANNEL +
                        Permissions.MANAGE_MESSAGES + Permissions.EMBED_LINKS + Permissions.READ_MESSAGE_HISTORY +
                        Permissions.USE_EXTERNAL_EMOJIS + Permissions.CHANGE_NICKNAME + Permissions.ATTACH_FILES).bits
            }"

            description = """
                I can help you play Avalon, Hangman, and (maybe soon-ish™) Exploding Kittens.
                
                To add me to a server, go to $url.
                
                I run on Andrew's Raspberry Pi - I will be online most of the time, but sometimes I do have power outages.
                
                My code can be found here: https://github.com/Andrew-Schwartz/AvalonBot.
            """.trimIndent()

            Bot.firstLogInTime?.let { timestamp(it) }
        }
    }
}