package lib.model

import com.google.gson.annotations.SerializedName

@Suppress("ArrayInDataClass")
data class Embed(
        val title: String? = null,
        val type: String? = null,
        val description: String? = null,
        val url: String? = null,
        val timestamp: Timestamp? = null,
        val color: Int? = null,
        val footer: EmbedFooter? = null,
        val image: EmbedImage? = null,
        val thumbnail: EmbedThumbnail? = null,
        val video: EmbedVideo? = null,
        val provider: EmbedProvider? = null,
        val author: EmbedAuthor? = null,
        val fields: Array<EmbedField>? = null
)

data class EmbedFooter(
        val text: String,
        @SerializedName("icon_url") val iconUrl: String,
        @SerializedName("proxy_icon_url") val proxyIconUrl: String
)

data class EmbedImage(
        val url: String? = null,
        @SerializedName("proxy_url") val proxyUrl: String? = null,
        val height: Int? = null,
        val width: Int? = null
)

data class EmbedThumbnail(
        val url: String?,
        @SerializedName("proxy_url") val proxyUrl: String?,
        val height: Int?,
        val width: Int?
)

data class EmbedVideo(
        val url: String?,
        val height: Int?,
        val width: Int?
)

data class EmbedProvider(
        val name: String?,
        val url: String?
)

data class EmbedAuthor(
        val name: String?,
        val url: String?,
        @SerializedName("icon_url") val iconUrl: String?,
        @SerializedName("proxy_icon_url") val proxyIconUrl: String?
)

data class EmbedField(
        val name: String,
        val value: String,
        val inline: Boolean?
)