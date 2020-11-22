package kittens.commands

import common.commands.MessageCommand
import common.commands.State
import common.game.GameType
import common.game.Setup
import io.ktor.util.*
import kittens.game.KittenConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.send
import lib.model.channel.Message

object ToggleImplodingCommand : MessageCommand(State.Setup.Setup) {
    override val name: String = "implode"

    override val description: String = "Toggles whether Exploding Kittens will use the exploding kittens expansion"

    override val usage: String = "implode"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val config = Setup[message.channel(), GameType.Kittens].config as KittenConfig
        config.implodingKittens = !config.implodingKittens
        message.channel().send("Imploding Kittens is now ${if (config.implodingKittens) "en" else "dis"}abled")
    }
}