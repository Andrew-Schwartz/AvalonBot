package lib.model

data class Reaction(
        val count: Int,
        val me: Boolean,
        val emoji: Emoji
)