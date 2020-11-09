package common.commands

import common.steadfast
import common.util.debug
import common.util.onNull
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.RichEmbed
import lib.dsl.embed
import lib.dsl.reply
import lib.model.Color
import lib.model.channel.Message
import lib.util.inlineCode
import java.time.Duration

object HelpCommand : MessageCommand(State.All) {
    override val name: String = "help"

    override val description: String = "sends setup help text, or specific help text if given a command name, such" +
            "as `addme`. Parameters documented by `usage` are required if surrounded" +
            "by angle brackets `<...>` and optional if surrounded by square brackets `[...]`"

    override val usage: String = "help [command (type)]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val args = message.args
        if (args.isEmpty()) {
            PaginatedHelp(message).send(Duration.ofMinutes(1))
        } else {
            val name = args[0].toLowerCase()
            messageCommands.firstOrNull { it.name == name }
                    ?.let { message.reply(embed = it.helpEmbed()) }
                    .onNull { message.reply("Unrecognized command. To learn more about a command, use `usage`.") }
        }
    }
}

class PaginatedHelp(message: Message) : Paginated(
        message.channelId,
        MessageCommand.messageCommands.asSequence()
                .filter { it.state !is State.Arbitrary }
                .filter { message.channelId.debug || it.state in message.channelId.states }
                .filter { message.channelId.debug || message.author == steadfast || !it.privileged }
                .groupBy { it.state }
                .map {
                    RichEmbed().apply {
                        title = "Help - Commands in ${it.key.name()}"
                        color = Color.gold
                        it.value.forEach {
                            addField("💡 ${it.name}", it.description)
                        }
                        footerText = "Use arrows to see commands applicable in other states"
                    }
                }
                .sortedBy { it.title!! }
                .toList()
)

suspend fun MessageCommand.helpEmbed(): RichEmbed = embed {
    title = "About `$name`"
    color = Color.gold
    addField("Description", this@helpEmbed.description, false)
    addField("Usage", "!${usage}".inlineCode())
    addField("Usable for", state.name())
}