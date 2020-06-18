package common.commands

import common.game.Game
import common.game.Setup
import common.steadfast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

object LogCommand : MessageCommand(State.All) {
    override val name: String = "log"

    override val description: String = "logs some variables"

    override val usage: String = "log"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        if (message.author == steadfast) {
            println("debug = $debug")
            println("states = ${Command._currentStates.mapKeys { it.key.name }}")
            println("Games: ")
            Game.games.flatMap { (channel, map) ->
                map.map { (type, game) -> Triple(type, channel, game) }
            }.forEach { (type, channel, game) ->
                println("$type in ${channel.name} = $game")
            }
            println("Setups: ")
            Setup.setups.flatMap { (channel, map) ->
                map.map { (type, setup) -> Triple(type, channel, setup) }
            }.forEach { (type, channel, setup) ->
                println("$type in ${channel.name} = $setup")
            }
            message.reply("logged to stdout")
        } else {
            message.reply("Only Andrew is that cool")
        }
    }
}