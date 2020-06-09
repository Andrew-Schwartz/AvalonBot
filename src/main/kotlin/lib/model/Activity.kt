package lib.model

import com.google.gson.annotations.SerializedName

/**
 * Bots can only send [name], [url], and [type]
 */
data class Activity(
        val name: String,
        val type: ActivityType,
        val url: String? = null,
        @SerializedName("created_at") val createdAt: Int? = null,
        val timestamps: Timestamps? = null,
        @SerializedName("application_id") val applicationId: ApplicationId? = null,
        val details: String? = null,
        val state: String? = null,
        val party: Party? = null,
        val assets: Assets? = null,
        val secrets: Secrets? = null,
        val instance: Boolean? = null,
        val flags: Int? = null
)

enum class ActivityType(val id: Int) {
    @SerializedName("0")
    Game(0),

    @SerializedName("1")
    Streaming(1),

    @SerializedName("2")
    Listening(2),

    @SerializedName("4")
    Custom(4)
}

data class Timestamps(
        val start: Long?,
        val end: Long?
)

@Suppress("ArrayInDataClass")
data class Party(
        val id: String?,
        @SerializedName("size") private val _size: Array<Int> // size[0] -> currSize, size[1] -> maxSize
) {
    val currSize: Int get() = _size[0]

    val maxSize: Int get() = _size[1]
}

data class Assets(
        @SerializedName("large_image") val largeImage: String?,
        @SerializedName("large_text") val largeText: String?,
        @SerializedName("small_image") val smallImage: String?,
        @SerializedName("small_text") val smallText: String?
)

data class Secrets(
        val join: String,
        val spectate: String,
        val match: String
)