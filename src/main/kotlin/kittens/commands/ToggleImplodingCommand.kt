package kittens.commands

import common.commands.Command
import common.commands.State
import common.game.GameType
import common.game.Setup
import kittens.game.KittensConfig
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

object ToggleImplodingCommand : Command(State.Setup) {
    override val name: String = "implode"

    override val description: String = "Toggles whether Exploding Kittens will use the exploding kittens expansion"

    override val usage: String = "implode"
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        val config = Setup[message.channel, GameType.ExplodingKittens].config as KittensConfig
        config.implodingKittens = !config.implodingKittens
        message.reply("Imploding Kittens is now ${if (config.implodingKittens) "en" else "dis"}abled")
    }
}