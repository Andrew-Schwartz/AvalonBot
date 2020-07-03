package hangman.commands

import common.commands.MessageCommand
import common.commands.State
import common.game.GameType
import common.game.Setup
import hangman.GuildHistWord
import hangman.WordnikWord
import hangman.game.HangmanConfig
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message

object HangmanCommand : MessageCommand(State.Setup.Setup) {
    override val name: String = "hangman"

    override val description: String = "Starts a game of Hangman, using "

    override val usage: String = "hangman [guild/web]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        message.guild()?.let {
            val config = Setup[message.channel(), GameType.Hangman].config as HangmanConfig
            config.randomWord = if (message.args.firstOrNull() == "web") {
                WordnikWord()
            } else {
                GuildHistWord.forGuild(it)
            }
            GameType.Hangman.startGame(message)
        }
    }
}