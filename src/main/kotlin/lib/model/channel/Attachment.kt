package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.model.Snowflake
import lib.model.Storable

data class Attachment(
        override val id: Snowflake,
        val filename: String,
        val size: Int,
        val url: String,
        @SerializedName("proxy_url") val proxyUrl: String,
        val height: Int?,
        val width: Int?
) : Storable {
    @Suppress("USELESS_ELVIS")
    override fun updateDataFrom(new: Storable?): Attachment {
        val a = (new as? Attachment) ?: throw IllegalArgumentException("Can only copy info from other attachments")

        return Attachment(
                a.id ?: id,
                a.filename ?: filename,
                a.size ?: size,
                a.url ?: url,
                a.proxyUrl ?: proxyUrl,
                a.height ?: height,
                a.width ?: width
        )
    }

    override fun equals(other: Any?): Boolean = (other as? Attachment)?.id == id

    override fun hashCode(): Int = id.hashCode()
}