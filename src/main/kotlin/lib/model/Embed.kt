package lib.model

import com.google.gson.annotations.SerializedName

@Suppress("ArrayInDataClass")
data class Embed(
        val title: String?,
        val type: String?,
        val description: String?,
        val url: String?,
        val timestamp: Timestamp?,
        val color: Int?,
        val footer: EmbedFooter,
        val image: EmbedImage,
        val thumbnail: EmbedThumbnail,
        val video: EmbedVideo,
        val provider: EmbedProvider,
        val author: EmbedAuthor,
        val fields: Array<EmbedField>
)

data class EmbedFooter(
        val text: String,
        @SerializedName("icon_url") val iconUrl: String,
        @SerializedName("proxy_icon_url") val proxyIconUrl: String
)

data class EmbedImage(
        val url: String?,
        @SerializedName("proxy_url") val proxyUrl: String?,
        val height: Int?,
        val width: Int?
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
        @SerializedName("icon_url") val iconUrl: String,
        @SerializedName("proxy_icon_url") val proxyIconUrl: String
)

data class EmbedField(
        val name: String,
        val value: String,
        val inline: Boolean?
)