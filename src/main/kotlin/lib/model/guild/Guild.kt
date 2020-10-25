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
        var name: String?,
        var icon: String?,
        var splash: String?,
        var owner: Boolean?,
        @SerializedName("owner_id") var ownerId: UserId,
        var permissions: Int?,
        var region: String,
        @SerializedName("afk_channel_id") var afkChannelId: ChannelId?,
        @SerializedName("afk_timeout") var afkTimeout: Int,
        @SerializedName("embed_enabled") var embedEnabled: Boolean?,
        @SerializedName("embed_channel_id") var embedChannelId: ChannelId?,
        @SerializedName("verification_level") var verificationLevel: Int,
        @SerializedName("default_message_notifications") var defaultMessageNotifications: Int,
        @SerializedName("explicit_content_filter") var explicitContentFilter: Int,
        var roles: Array<Role>,
        var emojis: Array<Emoji>,
        var features: Array<String>,
        @SerializedName("mfa_level") var mfaLevel: Int,
        @SerializedName("application_id") var applicationId: ApplicationId?,
        @SerializedName("widget_enabled") var widgetEnabled: Boolean?,
        @SerializedName("widget_channel_id") var widgetChannelId: ChannelId?,
        @SerializedName("system_channel_id") var systemChannelId: ChannelId?,
        @SerializedName("joined_at") var joinedAt: Timestamp?,
        var large: Boolean?,
        var unavailable: Boolean?,
        @SerializedName("member_count") var memberCount: Int?,
        @SerializedName("voice_states") var voiceStates: Array<VoiceState>?,
        var members: Array<GuildMember>?,
        var channels: Array<Channel>?,
        var presences: Array<PresenceUpdatePayload>?,
        @SerializedName("max_presences") var maxPresences: Int?,
        @SerializedName("max_members") var maxMembers: Int?,
        @SerializedName("vanity_url_code") var vanityUrlCode: String?,
        var description: String?,
        var banner: String?,
        @SerializedName("premium_tier") var premiumTier: PremiumTier,
        @SerializedName("premium_subscription_count") var premiumSubscriptionCount: Int?,
) : Storable<Guild>, IntoId<GuildId> by id {
    @Suppress("USELESS_ELVIS")
    override fun updateFrom(new: Guild) {
        name = new.name ?: name
        icon = new.icon ?: icon
        splash = new.splash ?: splash
        owner = new.owner ?: owner
        ownerId = new.ownerId ?: ownerId
        permissions = new.permissions ?: permissions
        region = new.region ?: region
        afkChannelId = new.afkChannelId ?: afkChannelId
        afkTimeout = new.afkTimeout ?: afkTimeout
        embedEnabled = new.embedEnabled ?: embedEnabled
        embedChannelId = new.embedChannelId ?: embedChannelId
        verificationLevel = new.verificationLevel ?: verificationLevel
        defaultMessageNotifications = new.defaultMessageNotifications ?: defaultMessageNotifications
        explicitContentFilter = new.explicitContentFilter ?: explicitContentFilter
        roles = new.roles ?: roles
        emojis = new.emojis ?: emojis
        features = new.features ?: features
        mfaLevel = new.mfaLevel ?: mfaLevel
        applicationId = new.applicationId ?: applicationId
        widgetEnabled = new.widgetEnabled ?: widgetEnabled
        widgetChannelId = new.widgetChannelId ?: widgetChannelId
        systemChannelId = new.systemChannelId ?: systemChannelId
        joinedAt = new.joinedAt ?: joinedAt
        large = new.large ?: large
        unavailable = new.unavailable ?: unavailable
        memberCount = new.memberCount ?: memberCount
        voiceStates = new.voiceStates ?: voiceStates
        members = new.members ?: members
        channels = new.channels ?: channels
        presences = new.presences ?: presences
        maxPresences = new.maxPresences ?: maxPresences
        maxMembers = new.maxMembers ?: maxMembers
        vanityUrlCode = new.vanityUrlCode ?: vanityUrlCode
        description = new.description ?: description
        banner = new.banner ?: banner
        premiumTier = new.premiumTier ?: premiumTier
        premiumSubscriptionCount = new.premiumSubscriptionCount ?: premiumSubscriptionCount
    }

    override fun equals(other: Any?): Boolean = (other as? Guild)?.id == id

    override fun hashCode(): Int = id.hashCode()
}
