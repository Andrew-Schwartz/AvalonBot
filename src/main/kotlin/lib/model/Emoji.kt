package lib.model

import com.google.gson.annotations.SerializedName

@Suppress("ArrayInDataClass")
data class Emoji(
        val id: Snowflake?,
        val name: String,
        val roles: Array<Snowflake>?,
        val user: User,
        @SerializedName("require_colons") val requireColons: Boolean?,
        val managed: Boolean?,
        val animated: Boolean?
)