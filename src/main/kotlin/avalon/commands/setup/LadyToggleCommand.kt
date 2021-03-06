package avalon.commands.setup

import avalon.game.AvalonConfig
import common.commands.MessageCommand
import common.commands.State
import common.game.GameType
import common.game.Setup
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.send
import lib.model.channel.Message

object LadyToggleCommand : MessageCommand(State.Setup.Setup) {
    override val name: String = "lady"

    override val description: String = "Enable or disable the Lady of the Lake. Starts disabled"

    override val usage: String = "lady [disable/enable]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val config = Setup[message.channel(), GameType.Avalon].config as AvalonConfig

        val args = message.args
        when {
            args.isEmpty() -> config.ladyEnabled = !config.ladyEnabled
            args[0].equals("disabled", true) -> config.ladyEnabled = false
            args[0].equals("enabled", true) -> config.ladyEnabled = true
            else -> config.ladyEnabled = !config.ladyEnabled
        }

        message.channel().send("The Lady of the Lake is now ${if (config.ladyEnabled) "en" else "dis"}abled")
    }
}