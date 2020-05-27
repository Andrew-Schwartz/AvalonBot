package kittens.commands

import common.commands.Command
import common.commands.State
import common.game.Game
import common.game.GameType
import kittens.game.ExplodingKittens
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
object HandCommand : Command(State.Kittens.Game) {
    override val name = "hand"

    override val description = "Lists the cards in your hand"

    override val usage = "hand"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, _ ->
        val state = (Game[message.channel, GameType.ExplodingKittens] as ExplodingKittens).state
        with(state) {
            message.author.sendDM(
                    userPlayerMap[message.author]?.hand
                            ?.sortedBy { it.name }
                            ?.joinToString(separator = "\n") { it.name }
                            ?: "You aren't in this game of exploding kittens, so you have no cards in your hand"
            )
        }
    }
}