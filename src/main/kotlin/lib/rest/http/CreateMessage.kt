package lib.rest.http

import com.google.gson.annotations.SerializedName
import lib.model.Embed
import lib.model.Snowflake
import java.io.InputStream

@Suppress("ArrayInDataClass")
data class CreateMessage(
        val content: String = "",
        val nonce: Snowflake? = null,
        val tts: Boolean = false,
        val file: Pair<String, InputStream>? = null,
//        val file: ByteArray = ByteArray(0),
        val embed: Embed? = null,
        @SerializedName("payload_json") val payloadJson: String? = null
)