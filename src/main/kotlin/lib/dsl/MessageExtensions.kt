package lib.dsl

import common.util.A
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.exceptions.PermissionException
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.httpRequests.*
import lib.util.ping

/**
 * Send a message in the same channel that this message was sent in. If [content] is set, that string will be sent
 * in the message. To send a [RichEmbed], either set [embed] or build the embed with the RichEmbed [builder]. Note:
 * [builder] takes [embed] as its receiver, so if both are set, the code in [builder] will determine what is send.
 * If [ping] is true, will @ the author of this [Message]. To @ any number of specific users, use [Channel.send].
 *
 * @return The [Message] object that was sent
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.reply(
        content: String = "",
        embed: RichEmbed = RichEmbed(),
        ping: Boolean = false,
        builder: suspend RichEmbed.() -> Unit = {},
): Message = channel().send(
        content = content,
        embed = embed,
        pingTargets = if (ping) A[author] else emptyArray(),
        builder = builder
)

/**
 * Edit a message. Can only edit messages sent by the bot. See [Channel.send] for descriptions of each parameter.
 *
 * @return The edited [Message] object
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.edit(
        content: String? = null,
        embed: RichEmbed? = null,
        pingTargets: Array<User> = emptyArray(),
        builder: (suspend RichEmbed.() -> Unit)? = null,
): Message {
    if (author != Bot.user) throw PermissionException("Can only edit messages you have sent")

    val pingText = pingTargets.joinToString(separator = "\n") { it.ping() }

    val text = when {
        content == null && pingTargets.isEmpty() -> null
        content == null -> pingText
        else -> pingText + content
    }

    val embed = when {
        embed == null && builder == null -> null
        embed == null -> RichEmbed().apply { builder!!.invoke(RichEmbed()) }.build()
        else -> embed.apply { builder?.invoke(this) }.build()
    }

    return editMessage(channelId, id, text, embed)
}

/**
 * Reacts to this [Message] with [emoji]
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.react(emoji: Char) {
    createReaction(channel(), id, emoji)
}

/**
 * @return Array of each user that reacted to this [Message] with [emoji].
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.reactions(emoji: Char) = getReactions(channelId, id, emoji)

/**
 * @return Arrays of each user that reacted to this [Message] with each emoji in the same order as [emojis].
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.reactions(vararg emojis: Char): List<Array<User>> = emojis.map { reactions(it) }

/**
 * Deletes this [Message]
 *
 * If you might be deleting more than one message at a time, use [deleteMessages] instead.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.delete() {
    deleteMessage(channelId, id)
}

/**
 * Delete any number of [messages] efficiently. Uses Discord's bulk delete api to delete groups of 100 messages at once
 * until all [messages] are deleted.
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteMessages(vararg messages: Message) {
    when (messages.size) {
        0 -> return
        1 -> {
            messages[0].delete()
        }
        else -> messages
                .groupBy { it.channelId }
                .forEach { (channelId, group) ->
                    group.chunked(100).forEach {
                        bulkDeleteMessages(channelId, it.toSet())
                    }
                }
    }
}

/**
 * Pins this message and saves it in [Bot]'s pinned message list
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.pin() {
    Bot.pinnedMessages += this
    addPin(channelId, id)
}

/**
 * Unpins this message and removes it from [Bot]'s pinned message list
 */
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.unpin() {
    Bot.pinnedMessages -= this
    deletePin(channelId, this)
}

/**
 * @return The [Channel] object this message is in
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Message.channel(): Channel = channelId.channel()

/**
 * @return The [Guild] object this message is in, or null if in a DM
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
suspend fun Message.guild(): Guild? = guildId?.guild()

/**
 * @return true if this message in in a DM channel, ie its guild is null
 */
val Message.isDM get() = guildId == null

/**
 * Gets the url link to this message, as you can get by right clicking in Discord.
 */
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun Message.link(): String {
    return if (isDM) {
        "https://discord.com/channels/@me/$channelId/$id"
    } else {
        "https://discord.com/channels/$guildId/$channelId/$id"
    }
}
