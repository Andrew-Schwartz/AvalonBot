package lib.rest.http

import com.google.gson.annotations.SerializedName
import lib.model.*
import lib.model.channel.Embed
import java.io.InputStream

@Suppress("ArrayInDataClass")
data class CreateMessage(
        val content: String = "",
        val nonce: Int? = null,
        val tts: Boolean = false,
        val file: Map<String, InputStream>? = null,
        val embed: Embed? = null,
        @SerializedName("allowed_mentions")
        val allowedMentions: AllowedMentions? = null,
        @SerializedName("message_reference")
        val messageReference: MessageReference? = null,
)

// TODO model mutual exclusivity
data class AllowedMentions(
        /**
         * An array of allowed mention types to parse from the content.
         */
        val parse: List<AllowedMentionType>?,
        /**
         * Array of role_ids to mention (Max size of 100)
         */
        val roles: List<RoleId>?,
        /**
         * Array of user_ids to mention (Max size of 100)
         */
        val users: List<UserId>?,
        /**
         * For replies, whether to mention the author of the message being replied to (default false)
         */
        @SerializedName("replied_user")
        val repliedUser: Boolean,
)

enum class AllowedMentionType {
    /**
     * Controls role mentions
     */
    @SerializedName("roles")
    Roles,

    /**
     * Controls user mentions
     */
    @SerializedName("users")
    Users,

    /**
     * Controls @everyone and @here mentions
     */
    @SerializedName("everyone")
    Everyone,
}

data class MessageReference(
        @SerializedName("message_id")
        val message: MessageId?,
        /**
         * channel_id is optional when creating a reply, but will always be present when receiving an event/response that includes this data model.
         */
        @SerializedName("channel_id")
        val channel: ChannelId? = null,
        @SerializedName("guild_id")
        val guild: GuildId?,
)