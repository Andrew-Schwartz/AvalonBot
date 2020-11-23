package lib.dsl

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.GuildId
import lib.model.IntoId
import lib.model.UserId
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.GuildMember
import lib.model.user.User
import lib.rest.http.httpRequests.createDM
import lib.rest.http.httpRequests.getGuildMember

/**
 * Creates or gets the DM channel between this bot and [User]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun IntoId<UserId>.getDM(): Channel = createDM(this)

/**
 * DMs a message to [User]. See [Channel.send] for parameter info.
 *
 * @return The [Message] object that was sent
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun IntoId<UserId>.sendDM(
        content: String = "",
        embed: RichEmbed = RichEmbed(),
        builder: suspend RichEmbed.() -> Unit = {},
): Message {
    return getDM().send(
            content = content,
            embed = embed,
            builder = builder
    )
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun IntoId<UserId>.getMember(id: IntoId<GuildId>): GuildMember = getGuildMember(id, this)