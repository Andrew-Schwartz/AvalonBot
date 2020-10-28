package hangman

import common.util.loop
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.time.delay
import lib.rest.client
import lib.util.fromJson
import java.time.Duration

@KtorExperimentalAPI
class WordnikWord : RandomWord {
    override suspend fun randomWord(): String {
        val url = "https://api.wordnik.com/v4/words.json/randomWord?api_key=$key"
        return loop {
            val word = client.get<HttpResponse>(url).fromJson<Word>().word
            if (word.all { it.isLetter() }) {
                return@loop word
            } else {
                delay(Duration.ofSeconds(1))
                null
            }
        }
    }

    @Suppress("JAVA_CLASS_ON_COMPANION")
    companion object {
        val key = javaClass.getResourceAsStream("/config/wordnik_key.txt")
                .bufferedReader()
                .readText()
    }
}

data class Word(
        val canonicalForm: String?,
        val id: Int,
        val originalWord: String?,
        val suggestions: List<Word>?,
        val vulgar: String?,
        val word: String,
)