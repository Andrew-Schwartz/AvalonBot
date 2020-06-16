package kittens.commands

import common.commands.MessageCommand
import common.commands.State
import common.game.GameType
import common.game.Setup
import io.ktor.util.KtorExperimentalAPI
import kittens.game.KittensConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

object ToggleImplodingCommand : MessageCommand(State.Setup.Setup) {
    override val name: String = "implode"

    override val description: String = "Toggles whether Exploding Kittens will use the exploding kittens expansion"

    override val usage: String = "implode"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val config = Setup[message.channel(), GameType.Kittens].config as KittensConfig
        config.implodingKittens = !config.implodingKittens
        message.reply("Imploding Kittens is now ${if (config.implodingKittens) "en" else "dis"}abled")
    }
}