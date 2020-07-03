package hangman.game

import common.bot
import common.game.Game
import common.game.GameFinish
import common.game.GameType
import common.game.Setup
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.suspendUntil
import lib.model.Color
import lib.model.channel.ChannelType
import lib.util.multilineCode

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Hangman(setup: Setup) : Game(GameType.Hangman, setup) {
    override val state = HangmanState(setup)

    override suspend fun runGame(): GameFinish {
        return bot.run game@{
            if (channel.type != ChannelType.GuildText) throw Exception("Hangman can only be played in guild channels (for now)")
            with(state) {
                word = randomWord.randomWord().toLowerCase()
                revealed = MutableList(word.length) { '_' }
                embed {
                    title = "The hangman is hungry: ${word.length} letter word"
                    description = asciiArt[0]?.multilineCode()
                    footerText = revealed.joinToString(separator = " ")
                }
                message = channel.send(embed = embed)
                message.react('❓')

                suspendUntil(500) { revealed.none { it == '_' } || bodyParts >= 5 }

                GameFinish {
                    if (bodyParts >= 5) {
                        title = "You lose and the hangman gets to eat"
                        color = Color.red
                    } else {
                        title = "You win!"
                        color = Color.gold
                    }
                    description = "The word was $word"
                }
            }
        }
    }
}