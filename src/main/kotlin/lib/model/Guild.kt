package lib.model

import com.google.gson.annotations.SerializedName

@Suppress("ArrayInDataClass")
data class Guild(
        override val id: Snowflake,
        val name: String,
        val icon: String?,
        val splash: String?,
        val owner: Boolean?,
        @SerializedName("owner_id") val ownerId: Snowflake,
        val permissions: Int?,
        val region: String,
        @SerializedName("afk_channel_id") val afkChannelId: Snowflake?,
        @SerializedName("afk_timeout") val afkTimeout: Int,
        @SerializedName("embed_enabled") val embedEnabled: Boolean?,
        @SerializedName("embed_channel_id") val embedChannelId: Snowflake?,
        @SerializedName("verification_level") val verificationLevel: Int,
        @SerializedName("default_message_notifications") val defaultMessageNotifications: Int,
        @SerializedName("explicit_content_filter") val explicitContentFilter: Int,
        val roles: Array<Role>,
        val emojis: Array<Emoji>,
        val features: Array<String>,
        @SerializedName("mfa_level") val mfaLevel: Int,
        @SerializedName("application_id") val applicationId: Snowflake?,
        @SerializedName("widget_enabled") val widgetEnabled: Boolean?,
        @SerializedName("widget_channel_id") val widgetChannelId: Snowflake?,
        @SerializedName("system_channel_id") val systemChannelId: Snowflake?,
        @SerializedName("joined_at") val joinedAt: Timestamp?,
        val large: Boolean?,
        val unavailable: Boolean?,
        @SerializedName("member_count") val memberCount: Int?,
        @SerializedName("voice_states") val voiceStates: Array<VoiceState>?,
        val members: Array<GuildMember>?,
        val channels: Array<Channel>?,
        val presences: Array<PresenceUpdatePayload>?,
        @SerializedName("max_presences") val maxPresences: Int?,
        @SerializedName("max_members") val maxMembers: Int?,
        @SerializedName("vanity_url_code") val vanityUrlCode: String?,
        val description: String?,
        val banner: String?,
        @SerializedName("premium_tier") val premiumTier: PremiumTier,
        @SerializedName("premium_subscription_count") val premiumSubscriptionCount: Int?
) : Storable

enum class PremiumTier {
    @SerializedName("0")
    None,
    @SerializedName("1")
    Tier1,
    @SerializedName("2")
    Tier2,
    @SerializedName("3")
    Tier3,
}

data class VoiceState(
        @SerializedName("guild_id") val guildId: Snowflake?,
        @SerializedName("channel_id") val channelId: Snowflake?,
        @SerializedName("user_id") val userId: Snowflake,
        val member: Guild?,
        val session_id: String,
        val deaf: Boolean,
        val mute: Boolean,
        @SerializedName("self_deaf") val selfDeaf: Boolean,
        @SerializedName("self_mute") val selfMute: Boolean,
        val suppress: Boolean
)

@Suppress("ArrayInDataClass")
data class PresenceUpdatePayload(
        val user: User,
        val roles: Array<Snowflake>,
        val game: Activity?,
        @SerializedName("guild_id") val guildId: Snowflake,
        val status: String,
        val activities: Array<Activity>,
        @SerializedName("client_status") val clientStatus: ClientStatus
)

data class ClientStatus(
        val desktop: String?,
        val mobile: String,
        val web: String?
)