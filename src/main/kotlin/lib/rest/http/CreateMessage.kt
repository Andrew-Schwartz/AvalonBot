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

sealed class AllowedMentionsStrategy {
    object Inferred : AllowedMentionsStrategy()
    class Explicit(val allowed: AllowedMentions) : AllowedMentionsStrategy()

    fun from(content: String): AllowedMentions? = when (this) {
        Inferred -> AllowedMentions.inferFrom(content)
        is Explicit -> this.allowed
    }.takeUnless { it == AllowedMentions() }
}

// TODO model mutual exclusivity
data class AllowedMentions(
        /**
         * An array of allowed mention types to parse from the content.
         */
        val parse: Set<AllowedMentionType>? = null,
        /**
         * Array of role_ids to mention (Max size of 100)
         */
        val roles: Set<RoleId>? = null,
        /**
         * Array of user_ids to mention (Max size of 100)
         */
        val users: Set<UserId>? = null,
        /**
         * For replies, whether to mention the author of the message being replied to (default false)
         */
        @SerializedName("replied_user")
        val repliedUser: Boolean = false,
) {
    companion object {
        // todo
        fun inferFrom(content: String, repliedUser: Boolean = false): AllowedMentions {
            var parse = emptySet<AllowedMentionType>()
            var roles = emptySet<RoleId>()
            var users = emptySet<UserId>()

            Regex("""<@([!&])(\d{16,19})>""").findAll(content)
                    .map { it.groupValues }
                    .forEach {
                        println("it = $it")
                    }

//            when (val char = find.groupValues.getOrNull(1)) {
//                null, "!" -> allowed = allowed.copy(users = L[])
//                "&" -> { }
//                else -> throw IllegalStateException("Matched $char instead of `!` or `&` in String `$content`")
//            }

            return AllowedMentions(
                    parse.takeUnless { it.isEmpty() },
                    roles.takeUnless { it.isEmpty() },
                    users.takeUnless { it.isEmpty() },
                    repliedUser
            )
        }
    }
}

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
        @Transient
        val ping: Boolean = false,
)