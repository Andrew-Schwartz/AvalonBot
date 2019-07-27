package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.model.Snowflake
import lib.model.Storable
import lib.model.Timestamp
import lib.model.guild.GuildMember
import lib.model.timestamp
import lib.model.user.User

@Suppress("ArrayInDataClass")
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
    override fun equals(other: Any?): Boolean = (other as? Message)?.id == id

    override fun hashCode(): Int = id.hashCode()

    val args: List<String>
        get() = content.split(" +".toRegex()).drop(1)

    @Suppress("USELESS_ELVIS", "UNNECESSARY_SAFE_CALL")
    override fun addNotNullDataFrom(new: Storable?): Message {
        val m = (new as? Message) ?: throw IllegalArgumentException("Can only copy info from other messages")

        return Message(
                m.id,
                m.channelId,
                m.guildId,
                m.author ?: author,
                m.member ?: member,
                m.content ?: content,
                m.timestamp.time?.timestamp() ?: timestamp,
                m.editedTimestamp ?: editedTimestamp,
                m.tts,
                m.mentionsEveryone,
                m.mentions ?: mentions,
                m.mentionRoles ?: mentionRoles,
                m.attachments ?: attachments,
                m.embeds ?: embeds, //TODO check this, some other part might be null
                m.reactions ?: reactions,
                m.nonce ?: nonce,
                m.pinned,
                m.webhookId ?: m.webhookId,
                m.type ?: type,
                m.activity ?: activity,
                m.application ?: application
        )
    }
}
