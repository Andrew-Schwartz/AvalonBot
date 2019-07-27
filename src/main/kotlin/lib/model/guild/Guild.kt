package lib.model.guild

import com.google.gson.annotations.SerializedName
import lib.model.Snowflake
import lib.model.Storable
import lib.model.Timestamp
import lib.model.channel.Channel
import lib.model.emoji.Emoji
import lib.model.permissions.Role
import lib.rest.model.events.receiveEvents.PresenceUpdatePayload

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
