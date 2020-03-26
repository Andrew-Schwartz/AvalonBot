package avalon.commands.setup

import common.commands.Command
import common.commands.CommandState.Setup
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Channel
import lib.model.channel.Message

object LadyToggleCommand : Command(Setup) {
    //    var enabled = false
    val enabled: MutableMap<Channel, Boolean> = mutableMapOf()

    override val name: String = "lady"

    override val description: String = "Enable or disable the Lady of the Lake. Starts disabled"

    override val usage: String = "!lady"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        enabled.compute(message.channel) { _, old -> !(old ?: false) }
        message.reply("The Lady of the Lake is now ${if (enabled[message.channel] == true) "en" else "dis"}abled")
    }
}