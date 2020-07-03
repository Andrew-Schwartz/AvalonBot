package hangman

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import lib.rest.client
import lib.util.fromJson

@KtorExperimentalAPI
class WordnikWord : RandomWord {
    override suspend fun randomWord(): String {
        val url = "https://api.wordnik.com/v4/words.json/randomWord?api_key=$key"
        val word: Word = client.get<HttpResponse>(url).fromJson()
        return word.word
    }

    @Suppress("JAVA_CLASS_ON_COMPANION")
    companion object {
        val key = javaClass.getResourceAsStream("/config/worknik_key.txt")
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
        val word: String
)