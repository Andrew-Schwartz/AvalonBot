package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.model.AttachmentId
import lib.model.IntoId
import lib.model.Storable

data class Attachment(
        override val id: AttachmentId,
        val filename: String,
        val size: Int,
        val url: String,
        @SerializedName("proxy_url") val proxyUrl: String,
        val height: Int?,
        val width: Int?
) : Storable<Attachment>, IntoId<AttachmentId> {
    override val prevVersions: MutableList<Attachment> = mutableListOf()

    @Suppress("USELESS_ELVIS")
    override fun updateDataFrom(new: Attachment?): Attachment {
        val a = new ?: return this

        return Attachment(
                a.id ?: id,
                a.filename ?: filename,
                a.size ?: size,
                a.url ?: url,
                a.proxyUrl ?: proxyUrl,
                a.height ?: height,
                a.width ?: width
        ).savePrev()
    }

    override fun equals(other: Any?): Boolean = (other as? Attachment)?.id == id

    override fun hashCode(): Int = id.hashCode()

    override fun intoId(): AttachmentId = id
}