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
    override fun addNotNullDataFrom(new: Storable?): Attachment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun equals(other: Any?): Boolean = (other as? Attachment)?.id == id

    override fun hashCode(): Int = id.hashCode()
}