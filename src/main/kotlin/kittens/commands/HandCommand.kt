package kittens.commands

import common.commands.MessageCommand
import common.commands.State
import io.ktor.util.KtorExperimentalAPI
import kittens.game.KittenState
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
        val state = KittenState.inChannel(message.channel())
        state?.run {
            message.author.sendDM(
                    userPlayerMap[message.author]?.hand
                            ?.sortedBy { it.name }
                            ?.joinToString(separator = "\n") { it.name }
                            ?: "You aren't in this game of exploding kittens, so you have no cards in your hand"
            )
        }
    }
}