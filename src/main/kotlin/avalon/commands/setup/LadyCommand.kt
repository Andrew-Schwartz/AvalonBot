package avalon.commands.setup

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import main.commands.Command
import main.commands.CommandState.Setup

object LadyCommand : Command(Setup) {
    var enabled = false

    override val name: String = "lady"

    override val description: String = "Enable or disable the Lady of the Lake. Starts disabled"

    override val usage: String = "!lady"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        enabled = !enabled
        message.reply("The Lady of the Lake is now ${if (enabled) "en" else "dis"}abled")
    }
}