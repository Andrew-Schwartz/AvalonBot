package lib.model.user

import com.google.gson.annotations.SerializedName
import lib.model.Snowflake
import lib.model.guild.Integration

@Suppress("ArrayInDataClass")
data class Connection(
        val id: Snowflake, // TODO maybe should be just string
        val name: String,
        val type: String,
        val revoked: Boolean,
        val integrations: Array<Integration>,
        val verified: Boolean,
        @SerializedName("friend_sync") val friendSync: Boolean,
        @SerializedName("show_activity") val showActivity: Boolean,
        val visibility: Visibility
)
