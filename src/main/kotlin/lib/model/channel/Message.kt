package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.model.*
import lib.model.guild.GuildMember
import lib.model.user.User

@Suppress("ArrayInDataClass")
data class Message(
        override val id: MessageId,
        @SerializedName("channel_id") val channelId: ChannelId,
        @SerializedName("guild_id") val guildId: GuildId?,
        var author: User,
        var member: GuildMember?,
        var content: String,
        val timestamp: Timestamp,
        @SerializedName("edited_timestamp") var editedTimestamp: Timestamp?,
        var tts: Boolean,
        @SerializedName("mention_everyone") var mentionsEveryone: Boolean,
        var mentions: Array<User>,
        @SerializedName("mention_roles") private var mentionRoles: List<RoleId>,
        var attachments: Array<Attachment>,
        var embeds: Array<Embed>,
        var reactions: Array<Reaction>?,
        var nonce: String?,
        var pinned: Boolean,
        @SerializedName("webhook_id") var webhookId: WebhookId?,
        var type: MessageType,
        var activity: MessageActivity,
        var application: MessageApplication,
) : Storable<Message>, IntoId<MessageId> by id {
    override fun equals(other: Any?): Boolean = (other as? Message)?.id == id

    override fun hashCode(): Int = id.hashCode()

    val args: List<String>
        get() = content.split("\\s+".toRegex()).drop(1)

    @Suppress("USELESS_ELVIS", "UNNECESSARY_SAFE_CALL", "SENSELESS_COMPARISON")
    override fun updateFrom(new: Message) {
        if (author == null) {
            author = new.author
        } else {
            new.author?.let { author updateFrom it }
        }
        member = new.member ?: member
        content = new.content ?: content
        editedTimestamp = new.editedTimestamp ?: editedTimestamp
        tts = new.tts ?: tts
        mentionsEveryone = new.mentionsEveryone ?: mentionsEveryone
        mentions = new.mentions ?: mentions
        mentionRoles = new.mentionRoles ?: mentionRoles
        attachments = new.attachments ?: attachments
        embeds = new.embeds ?: embeds //TODO check this, some other part might be null
        reactions = new.reactions ?: reactions
        nonce = new.nonce ?: nonce
        pinned = new.pinned ?: pinned
        webhookId = new.webhookId ?: webhookId
        type = new.type ?: type
        activity = new.activity ?: activity
        application = new.application ?: application
    }
}