package common.commands

import common.game.Game
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
            println("debug = $debug")
            println("Games: ")
            Game.games.flatMap { (channel, map) ->
                map.map { (type, game) -> Triple(type, channel, game) }
            }.forEach { (type, channel, game) ->
                println("$type in ${channel.name} = $game")
            }
//            for (gameType in GameType.values()) {
//                Game.games.println("$gameType = ${Game.games}")
//            }
            println("Setups: ")
            Setup.setups.flatMap { (channel, map) ->
                map.map { (type, setup) -> Triple(type, channel, setup) }
            }.forEach { (type, channel, setup) ->
                println("$type in ${channel.name} = $setup")
            }
//            for (gameType in GameType.values()) {
//                println("$gameType = ${Setup.setups}")
//            }
            message.reply("logged to stdout")
        } else {
            message.reply("Only Andrew is that cool")
        }
    }
}