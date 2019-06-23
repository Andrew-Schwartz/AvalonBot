package lib.rest.http

import lib.model.Embed
import lib.model.Snowflake
import java.io.InputStream

@Suppress("ArrayInDataClass")
data class CreateMessage(
        val content: String = "",
        val nonce: Snowflake? = null,
        val tts: Boolean = false,
        val file: Map<String, InputStream>? = null,
        val embed: Embed? = null
)