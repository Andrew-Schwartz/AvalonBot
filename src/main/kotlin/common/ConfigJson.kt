package common

import lib.model.ChannelId
import lib.model.UserId

data class ConfigJson(
        val token: String,
        val prefix: String,
        val steadfastId: UserId,
        val ktsId: ChannelId
)