package lib.model

import com.google.gson.annotations.SerializedName

data class Attachment(
        override val id: Snowflake,
        val filename: String,
        val size: Int,
        val url: String,
        @SerializedName("proxy_url") val proxyUrl: String,
        val height: Int?,
        val width: Int?
) : Storable