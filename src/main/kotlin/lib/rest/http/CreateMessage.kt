package lib.rest.http

import lib.model.channel.Embed
import java.io.InputStream

@Suppress("ArrayInDataClass")
data class CreateMessage(
        val content: String = "",
        val nonce: String? = null,
        val tts: Boolean = false,
        val file: Map<String, InputStream>? = null,
        val embed: Embed? = null
)