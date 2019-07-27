package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.dsl.DiscordDsl
import lib.model.Snowflake
import lib.model.Storable
import lib.model.Timestamp
import lib.model.guild.GuildMember
import lib.model.user.User

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
