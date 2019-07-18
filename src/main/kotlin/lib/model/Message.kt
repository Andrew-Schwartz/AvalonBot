package lib.model

import com.google.gson.annotations.SerializedName
import lib.dsl.DiscordDsl

@Suppress("ArrayInDataClass")
@DiscordDsl
data class Message(
        override val id: Snowflake,
        @SerializedName("channel_id") val channelId: Snowflake,
        @SerializedName("guild_id") val guildId: Snowflake?,
        val author: User,
        val member: GuildMember,
        val content: String,
        val timestamp: Timestamp,
        @SerializedName("edited_timestamp") val editedTimestamp: Timestamp?,
        val tts: Boolean,
        @SerializedName("mention_everyone") val mentionsEveryone: Boolean,
        val mentions: Array<User>,
        @SerializedName("mention_roles") val mentionRoles: Array<Snowflake>,
        val attachments: Array<Attachment>,
        val embeds: Array<Embed>,
        val reactions: Array<Reaction>?,
        val nonce: Snowflake?,
        val pinned: Boolean,
        @SerializedName("webhook_id") val webhookId: Snowflake?,
        val type: MessageType,
        val activity: MessageActivity,
        val application: MessageApplication
) : Storable {
    val args: List<String>
        get() = content.split(" +".toRegex()).drop(1)
}

enum class MessageType {
    @SerializedName("0")
    Default,
    @SerializedName("1")
    RecipientAdd,
    @SerializedName("2")
    RecipientRemove,
    @SerializedName("3")
    Call,
    @SerializedName("4")
    ChannelNameChange,
    @SerializedName("5")
    ChannelIconChange,
    @SerializedName("6")
    ChannelPinnedChange,
    @SerializedName("7")
    GuildMemberJoin,
    @SerializedName("8")
    UserPremiumGuildSubscription,
    @SerializedName("9")
    UserPremiumGuildSubscriptionTier1,
    @SerializedName("10")
    UserPremiumGuildSubscriptionTier2,
    @SerializedName("11")
    UserPremiumGuildSubscriptionTier3
}

data class MessageActivity(
        val type: MessageActivityType,
        @SerializedName("party_id") val partyId: String
)

enum class MessageActivityType {
    @SerializedName("1")
    Join,
    @SerializedName("2")
    Spectate,
    @SerializedName("3")
    Listen,
    @SerializedName("5")
    JoinRequest
}

data class MessageApplication(
        val id: Snowflake,
        @SerializedName("cover_image") val coverImage: String,
        val description: String,
        val icon: String?,
        val name: String
)