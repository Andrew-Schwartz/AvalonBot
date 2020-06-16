package kittens.commands

import common.commands.MessageCommand
import common.commands.State
import common.game.Game
import common.game.GameType
import io.ktor.util.KtorExperimentalAPI
import kittens.game.ExplodingKittens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
object HandCommand : MessageCommand(State.Kittens.Game) {
    override val name = "hand"

    override val description = "Lists the cards in your hand"

    override val usage = "hand"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val state = (Game[message.channel(), GameType.Kittens] as ExplodingKittens).state
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