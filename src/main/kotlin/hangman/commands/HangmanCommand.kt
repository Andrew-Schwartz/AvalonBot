package hangman.commands

import common.commands.MessageCommand
import common.commands.State
import common.game.GameType
import common.game.Setup
import hangman.GuildHistWord
import hangman.WordnikWord
import hangman.game.HangmanConfig
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.guild
import lib.dsl.startTyping
import lib.model.channel.Message

object HangmanCommand : MessageCommand(State.Setup.Setup) {
    override val name: String = "hangman"

    override val description: String = "Starts a game of Hangman, using a random word from this server or from an online" +
            "service (specify `web`)"

    override val usage: String = "hangman [web]"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val config = Setup[message.channel(), GameType.Hangman].config as HangmanConfig
        config.randomWord = message.guild()?.let {
            if (message.args.firstOrNull() == "web") {
                WordnikWord()
            } else {
                if (GuildHistWord.noGuildData(it)) {
                    // gathering words takes a bit the first time
                    message.channel().startTyping()
                }
                GuildHistWord.forGuild(it)
            }
        } ?: WordnikWord()

//        StartCommand.execute()
        GameType.Hangman.startGame(message)
    }
}