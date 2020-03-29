package avalon.commands.setup

import avalon.game.AvalonConfig
import common.commands.Command
import common.commands.CommandState
import common.game.GameType
import common.game.Setup
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

object LadyToggleCommand : Command(CommandState.Setup) {
    override val name: String = "lady"

    override val description: String = "Enable or disable the Lady of the Lake. Starts disabled"

    override val usage: String = "!lady [disable/enable]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val config = Setup[message.channel, GameType.Avalon].config as AvalonConfig
        when {
            args.isEmpty() -> config.ladyEnabled = !config.ladyEnabled
            args[0].equals("disabled", true) -> config.ladyEnabled = false
            args[0].equals("enabled", true) -> config.ladyEnabled = true
            else -> config.ladyEnabled = !config.ladyEnabled
        }

        message.reply("The Lady of the Lake is now ${if (config.ladyEnabled) "en" else "dis"}abled")
    }
}