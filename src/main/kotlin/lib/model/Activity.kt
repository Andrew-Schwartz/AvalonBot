package lib.model

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import lib.model.emoji.Emoji
import java.lang.reflect.Type

/**
 * Bots can only send [name], [url], and [type]
 */
data class Activity(
        val name: String,
        /**
         * Determines how the status will be formatted. See [ActivityType] for more details.
         */
        val type: ActivityType,
        val url: String? = null,
        @SerializedName("created_at") val createdAt: Int? = null,
        val timestamps: Timestamps? = null,
        @SerializedName("application_id") val applicationId: ApplicationId? = null,
        val details: String? = null,
        val state: String? = null,
        val emoji: Emoji? = null,
        val party: Party? = null,
        val assets: Assets? = null,
        val secrets: Secrets? = null,
        val instance: Boolean? = null,
        val flags: Int? = null,
)

/**
 * Each enum value will format the activity differently
 */
enum class ActivityType(val id: Int) {
    /**
     * Playing $name
     */
    @SerializedName("0")
    Game(0),

    /**
     * Streaming $detail
     */
    @SerializedName("1")
    Streaming(1),

    /**
     * Listening to $name
     */
    @SerializedName("2")
    Listening(2),

    /**
     * $emoji $name
     */
    @SerializedName("4")
    Custom(4),

    /**
     * Competing in {name}
     */
    @SerializedName("5")
    Competing(5),
}

internal class ActivityTypeAdapter : JsonSerializer<ActivityType> {
    override fun serialize(src: ActivityType, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(src.id)
}

data class Timestamps(
        val start: Long?,
        val end: Long?,
)

@Suppress("ArrayInDataClass")
data class Party(
        val id: String?,
        @SerializedName("size") private val _size: Array<Int>, // size[0] -> currSize, size[1] -> maxSize
) {
    val currSize: Int get() = _size[0]

    val maxSize: Int get() = _size[1]
}

data class Assets(
        @SerializedName("large_image") val largeImage: String?,
        @SerializedName("large_text") val largeText: String?,
        @SerializedName("small_image") val smallImage: String?,
        @SerializedName("small_text") val smallText: String?,
)

data class Secrets(
        val join: String,
        val spectate: String,
        val match: String,
)