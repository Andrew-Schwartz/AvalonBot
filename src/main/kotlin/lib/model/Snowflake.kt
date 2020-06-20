package lib.model

import common.bot
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.httpRequests.getChannel
import lib.rest.http.httpRequests.getGuild
import lib.rest.http.httpRequests.getUser

interface Snowflake {
    val value: String
}

interface IntoId<T : Snowflake?> {
    fun intoId(): T
}

inline class GuildId(override val value: String) : Snowflake, IntoId<GuildId> {
    override fun intoId(): GuildId = this

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun guild(): Guild = bot.getGuild(this)

    override fun toString(): String = value
}

inline class ChannelId(override val value: String) : Snowflake, IntoId<ChannelId> {
    override fun intoId(): ChannelId = this

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun channel(): Channel = bot.getChannel(this)

    override fun toString(): String = value
}

inline class UserId(override val value: String) : Snowflake, IntoId<UserId> {
    override fun intoId(): UserId = this

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    suspend fun user(): User = bot.getUser(this)

    override fun toString(): String = value
}

inline class MessageId(override val value: String) : Snowflake, IntoId<MessageId> {
    override fun intoId(): MessageId = this

    override fun toString(): String = value
}

inline class AttachmentId(override val value: String) : Snowflake, IntoId<AttachmentId> {
    override fun intoId(): AttachmentId = this

    override fun toString(): String = value
}

inline class ApplicationId(override val value: String) : Snowflake, IntoId<ApplicationId> {
    override fun intoId(): ApplicationId = this

    override fun toString(): String = value
}

inline class WebhookId(override val value: String) : Snowflake, IntoId<WebhookId> {
    override fun intoId(): WebhookId = this

    override fun toString(): String = value
}

inline class EmojiId(override val value: String) : Snowflake, IntoId<EmojiId> {
    override fun intoId(): EmojiId = this

    override fun toString(): String = value
}

inline class RoleId(override val value: String) : Snowflake, IntoId<RoleId> {
    override fun intoId(): RoleId = this

    override fun toString(): String = value
}

inline class IntegrationId(override val value: String) : Snowflake, IntoId<IntegrationId> {
    override fun intoId(): IntegrationId = this

    override fun toString(): String = value
}

// either a user or a role, for permissions
inline class UserRoleId(override val value: String) : Snowflake, IntoId<UserRoleId> {
    override fun intoId(): UserRoleId = this

    override fun toString(): String = value
}