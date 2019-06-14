package lib.rest.http

import com.google.gson.annotations.SerializedName
import lib.model.Embed
import lib.model.Snowflake

data class CreateMessage(
        val content: String = "",
        val nonce: Snowflake? = null,
        val tts: Boolean = false,
//        val file: File? = null
        val embed: Embed? = null,
        @SerializedName("payload_json") val payloadJson: String? = null
)