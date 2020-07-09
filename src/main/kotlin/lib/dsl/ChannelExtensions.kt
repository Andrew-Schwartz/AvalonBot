package lib.dsl

import common.util.listGrammatically
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.createMessage
import lib.rest.http.httpRequests.getMessage
import lib.rest.http.httpRequests.triggerTypingIndicator
import lib.util.ping

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.send(
        content: String = "",
        embed: RichEmbed = RichEmbed(),
        pingTargets: Array<User> = emptyArray(),
        builder: suspend RichEmbed.() -> Unit = {}
): Message {
    val text = pingTargets.joinToString(separator = "\n", postfix = content) { it.ping() }

    @Suppress("NAME_SHADOWING")
    val embed = embed.apply { builder() }.takeIf { it.isNotEmpty }?.build()

    return createMessage(this, CreateMessage(
            content = text,
            embed = embed,
            file = embed?.files
    ))
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.startTyping() {
    triggerTypingIndicator(id)
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.lastMessage(): Message? {
    return getMessage(id, lastMessageId ?: return null)
}


@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.guild(): Guild? = guildId?.guild()

/**
 * In a guild, gets the channel name:
 *
 * ex: "AwesomeGuild/BotChannel"
 *
 * In a DM, gets the target user(s)
 *
 * ex: "DM: Bob the Builder(, Sally, and George Washington)"
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.fullName(): String {
    return guild()?.let {
        "${it.name}/$name"
    } ?: "DM: ${recipients?.listGrammatically { it.username }}"
}