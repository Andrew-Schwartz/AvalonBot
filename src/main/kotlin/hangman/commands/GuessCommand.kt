package hangman.commands

import common.commands.ReactCommand
import common.commands.State
import common.util.L
import hangman.game.HangmanState
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import lib.util.multilineCode

object GuessCommand : ReactCommand(State.Hangman.Game) {
    override val emojis: List<String> = L[
            "🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯", "🇰", "🇱", "🇲",
            "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹", "🇺", "🇻", "🇼", "🇽", "🇾", "🇿"
    ]

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(MessageReactionUpdatePayload) -> Unit = { reaction ->
        val state = HangmanState.inChannel(reaction.channel())
        state?.run {
            if (bodyParts >= 5) return@run
            if (revealed.none { it == '_' }) return@run
            val letter = getLetter(reaction.emoji.name) ?: return@run
            if (letter in guesses) return@run
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