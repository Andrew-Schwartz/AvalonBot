package lib.model.user

import com.google.gson.annotations.SerializedName
import lib.model.guild.Integration

/**
 * @see [https://discord.com/developers/docs/resources/user#get-current-user]
 */
@Suppress("ArrayInDataClass")
data class Connection(
        val id: String,
        val name: String,
        val type: String,
        val revoked: Boolean,
        val integrations: Array<Integration>,
        val verified: Boolean,
        @SerializedName("friend_sync") val friendSync: Boolean,
        @SerializedName("show_activity") val showActivity: Boolean,
        val visibility: Visibility,
)
