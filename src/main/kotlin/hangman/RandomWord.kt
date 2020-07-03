package hangman

interface RandomWord {
    suspend fun randomWord(): String
}