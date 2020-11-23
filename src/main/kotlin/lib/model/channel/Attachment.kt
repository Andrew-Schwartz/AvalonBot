package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.model.AttachmentId
import lib.model.IntoId
import lib.model.Storable

data class Attachment(
        val id: AttachmentId,
        var filename: String,
        var size: Int,
        var url: String,
        @SerializedName("proxy_url") var proxyUrl: String,
        var height: Int?,
        var width: Int?,
) : Storable<AttachmentId, Attachment>, IntoId<AttachmentId> by id {
    @Suppress("USELESS_ELVIS")
    override fun updateFrom(new: Attachment) {
        filename = new.filename ?: filename
        size = new.size ?: size
        url = new.url ?: url
        proxyUrl = new.proxyUrl ?: proxyUrl
        height = new.height ?: height
        width = new.width ?: width
    }

    override fun equals(other: Any?): Boolean = (other as? Attachment)?.id == id

    override fun hashCode(): Int = id.hashCode()
}