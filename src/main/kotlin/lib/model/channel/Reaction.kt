package lib.model.channel

import lib.model.emoji.Emoji

data class Reaction(
        val count: Int,
        val me: Boolean,
        val emoji: Emoji,
)