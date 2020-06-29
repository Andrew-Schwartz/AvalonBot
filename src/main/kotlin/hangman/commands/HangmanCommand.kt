package hangman.commands

import common.commands.MessageCommand
import common.commands.State
import common.game.Game
import common.game.GameType
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

object HangmanCommand : MessageCommand(State.Setup.Setup) {
    override val name: String = "hangman"

    override val description: String = "starts a game of Hangman!"

    override val usage: String = "hangman"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        val game = Game[message.channel(), GameType.Hangman]
        GameType.Hangman.startGame(message)
    }
}