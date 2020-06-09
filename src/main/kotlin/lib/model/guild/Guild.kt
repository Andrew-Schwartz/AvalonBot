package lib.model.guild

import com.google.gson.annotations.SerializedName
import lib.model.*
import lib.model.channel.Channel
import lib.model.emoji.Emoji
import lib.model.permissions.Role
import lib.rest.model.events.receiveEvents.PresenceUpdatePayload

@Suppress("ArrayInDataClass")
data class Guild(
        override val id: GuildId,
        val name: String?,
        val icon: String?,
        val splash: String?,
        val owner: Boolean?,
        @SerializedName("owner_id") val ownerId: UserId,
        val permissions: Int?,
        val region: String,
        @SerializedName("afk_channel_id") val afkChannelId: ChannelId?,
        @SerializedName("afk_timeout") val afkTimeout: Int,
        @SerializedName("embed_enabled") val embedEnabled: Boolean?,
        @SerializedName("embed_channel_id") val embedChannelId: ChannelId?,
        @SerializedName("verification_level") val verificationLevel: Int,
        @SerializedName("default_message_notifications") val defaultMessageNotifications: Int,
        @SerializedName("explicit_content_filter") val explicitContentFilter: Int,
        val roles: Array<Role>,
        val emojis: Array<Emoji>,
        val features: Array<String>,
        @SerializedName("mfa_level") val mfaLevel: Int,
        @SerializedName("application_id") val applicationId: ApplicationId?,
        @SerializedName("widget_enabled") val widgetEnabled: Boolean?,
        @SerializedName("widget_channel_id") val widgetChannelId: ChannelId?,
        @SerializedName("system_channel_id") val systemChannelId: ChannelId?,
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
) : Storable<Guild>, IntoId<GuildId> {
    @Suppress("USELESS_ELVIS")
    override fun updateDataFrom(new: Guild?): Guild {
        val g = new ?: return this

        return Guild(
                g.id ?: id,
                g.name ?: name,
                g.icon ?: icon,
                g.splash ?: splash,
                g.owner ?: owner,
                g.ownerId ?: ownerId,
                g.permissions ?: permissions,
                g.region ?: region,
                g.afkChannelId ?: afkChannelId,
                g.afkTimeout ?: afkTimeout,
                g.embedEnabled ?: embedEnabled,
                g.embedChannelId ?: embedChannelId,
                g.verificationLevel ?: verificationLevel,
                g.defaultMessageNotifications ?: defaultMessageNotifications,
                g.explicitContentFilter ?: explicitContentFilter,
                g.roles ?: roles,
                g.emojis ?: emojis,
                g.features ?: features,
                g.mfaLevel ?: mfaLevel,
                g.applicationId ?: applicationId,
                g.widgetEnabled ?: widgetEnabled,
                g.widgetChannelId ?: widgetChannelId,
                g.systemChannelId ?: systemChannelId,
                g.joinedAt ?: joinedAt,
                g.large ?: large,
                g.unavailable ?: unavailable,
                g.memberCount ?: memberCount,
                g.voiceStates ?: voiceStates,
                g.members ?: members,
                g.channels ?: channels,
                g.presences ?: presences,
                g.maxPresences ?: maxPresences,
                g.maxMembers ?: maxMembers,
                g.vanityUrlCode ?: vanityUrlCode,
                g.description ?: description,
                g.banner ?: banner,
                g.premiumTier ?: premiumTier,
                g.premiumSubscriptionCount ?: premiumSubscriptionCount
        ).savePrev()
    }

    override val prevVersions: MutableList<Guild> = mutableListOf()

    override fun equals(other: Any?): Boolean = (other as? Guild)?.id == id

    override fun hashCode(): Int = id.hashCode()
    override fun intoId(): GuildId = this.id
}
