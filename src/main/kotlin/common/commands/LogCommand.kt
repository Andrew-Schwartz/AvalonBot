package common.commands

import common.game.Game
import common.game.GameType
import common.game.Setup
import common.steadfast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

object LogCommand : Command(State.All) {
    override val name: String = "log"

    override val description: String = "logs some variables"

    override val usage: String = "log"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        if (message.author == steadfast) {
            println("\ndebug = $debug")
            println("Games: ")
            for (gameType in GameType.values()) {
                println("$gameType = ${Game.games}")
            }
            println("Setups: ")
            for (gameType in GameType.values()) {
                println("$gameType = ${Setup.setups}")
            }
            message.reply("logged to stdout")
        } else {
            message.reply("Only Andrew is that cool")
        }
    }
}