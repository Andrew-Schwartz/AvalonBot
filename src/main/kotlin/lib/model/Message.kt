package lib.model

import com.google.gson.annotations.SerializedName
import lib.dsl.DiscordDsl

@Suppress("ArrayInDataClass")
@DiscordDsl
data class Message(
        val id: Snowflake,
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
        val type: Int,
        val activity: MessageActivity,
        val application: MessageApplication
)

data class MessageActivity(
        val type: Int,
        @SerializedName("party_id") val partyId: String
)

data class MessageApplication(
        val id: Snowflake,
        @SerializedName("cover_image") val coverImage: String,
        val description: String,
        val icon: String?,
        val name: String
)