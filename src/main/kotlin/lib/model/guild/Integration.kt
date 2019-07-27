package lib.model.guild

import com.google.gson.annotations.SerializedName
import lib.model.Snowflake
import lib.model.Timestamp
import lib.model.user.User

data class Integration(
        val id: Snowflake,
        val name: String,
        val type: String,
        val enabled: Boolean,
        val syncing: Boolean,
        @SerializedName("role_id") val roleId: Snowflake,
        @SerializedName("expire_behavior") val expireBehavior: Int,
        @SerializedName("expire_grace_period") val expireGracePeriod: Int,
        val user: User,
        val account: Account,
        @SerializedName("synced_at") val syncedAt: Timestamp
)
