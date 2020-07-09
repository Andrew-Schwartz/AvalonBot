package hangman.commands

import common.commands.ReactCommand
import common.commands.State
import common.game.Game
import common.game.GameType
import common.util.L
import hangman.game.HangmanState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.edit
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import lib.util.multilineCode

object GuessCommand : ReactCommand(State.Hangman.Game) {
    override val emojis: List<String> = L[
            "🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯", "🇰", "🇱", "🇲",
            "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹", "🇺", "🇻", "🇼", "🇽", "🇾", "🇿"
    ]

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (MessageReactionUpdatePayload) -> Unit = { reaction ->
        val state = Game[reaction.channel(), GameType.Hangman].state as HangmanState
        with(state) {
            if (bodyParts >= 5) return@with
            if (revealed.none { it == '_' }) return@with
            val letter = getLetter(reaction.emoji.name) ?: return@with
            if (letter in guesses) return@with
            guesses += letter
            val indices = word.withIndex()
                    .filter { (_, c) -> c == letter }
                    .onEach { (i, c) -> revealed[i] = c }
                    .map { (i, _) -> i }
            if (indices.isEmpty()) bodyParts++
            state.message.edit(embed = embed {
                description = asciiArt[bodyParts]?.multilineCode()
                footerText = """
                    There ${
                when (val n = indices.size) {
                    1 -> "is 1 $letter"
                    else -> "are $n $letter's"
                }
                } in the word.
                    ${revealed.joinToString(" ")}
                """.trimIndent()
            })
        }
    }

    private fun getLetter(emoji: String) = when (emoji) {
        "🇦" -> 'a'
        "🇧" -> 'b'
        "🇨" -> 'c'
        "🇩" -> 'c'
        "🇪" -> 'e'
        "🇫" -> 'f'
        "🇬" -> 'g'
        "🇭" -> 'h'
        "🇮" -> 'i'
        "🇯" -> 'j'
        "🇰" -> 'k'
        "🇱" -> 'l'
        "🇲" -> 'm'
        "🇳" -> 'n'
        "🇴" -> 'o'
        "🇵" -> 'p'
        "🇶" -> 'q'
        "🇷" -> 'r'
        "🇸" -> 's'
        "🇹" -> 't'
        "🇺" -> 'u'
        "🇻" -> 'v'
        "🇼" -> 'w'
        "🇽" -> 'x'
        "🇾" -> 'y'
        "🇿" -> 'z'
        else -> null
    }
}