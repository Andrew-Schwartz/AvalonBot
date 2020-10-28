package lib.dsl

import common.util.listGrammatically
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.MessageId
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.CreateMessage
import lib.rest.http.httpRequests.createMessage
import lib.rest.http.httpRequests.getMessage
import lib.rest.http.httpRequests.triggerTypingIndicator
import lib.util.ping

/**
 * Send a message in this [Channel]. If [content] is set, that string will be sent in the message. To send a
 * [RichEmbed], either set [embed] or build the embed with the RichEmbed [builder]. Note: [builder] takes [embed] as its
 * receiver, so if both are set, the code in [builder] will determine what is send. Will @ every user in [pingTargets].
 *
 * @return The [Message] object that was sent
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.send(
        content: String = "",
        embed: RichEmbed = RichEmbed(),
        pingTargets: Array<User> = emptyArray(),
        builder: suspend RichEmbed.() -> Unit = {},
): Message {
    val text = pingTargets.joinToString(separator = "\n", postfix = content) { it.ping() }

    @Suppress("NAME_SHADOWING")
    val embed = embed.apply { builder() }.takeIf { !it.isEmpty }?.build()

    return createMessage(this, CreateMessage(
            content = text,
            embed = embed,
            file = embed?.files
    ))
}

/**
 * Start the typing indicator in this [Channel]. Will stay active until the bot sends a message in this channel or 15
 * seconds pass. Should not be used often.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.startTyping() {
    triggerTypingIndicator(id)
}

/**
 * @return The most recent message sent in this channel, if known.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.lastMessage(): Message? {
    return getMessage(id, lastMessageId ?: return null)
}

/**
 * @return The message [id] in this channel
 * @throws Exception An exception presumably if no message with id [id] exists in this channel.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Channel.getMessage(id: MessageId): Message {
    return getMessage(this, id)
}

/**
 * @return The [Guild] object this channel is part of, or null if in a DM
 */
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