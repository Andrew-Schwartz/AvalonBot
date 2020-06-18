package common.util

import lib.model.channel.Message

data class Vote(
        val message: Message,
        var score: Int = 0
)