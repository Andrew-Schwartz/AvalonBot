package lib.dsl

import common.util.A
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.exceptions.PermissionException
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.httpRequests.*
import lib.util.ping

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.reply(
        content: String = "",
        embed: RichEmbed = RichEmbed(),
        ping: Boolean = false,
        builder: suspend RichEmbed.() -> Unit = {}
): Message = channel().send(
        content = content,
        embed = embed,
        pingTargets = if (ping) A[author] else emptyArray(),
        builder = builder
)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.edit(
        content: String? = null,
        embed: RichEmbed? = null,
        pingTargets: Array<User> = emptyArray(),
        builder: (suspend RichEmbed.() -> Unit)? = null
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

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.react(emoji: Char) {
    createReaction(channel(), id, emoji)
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.reactions(emoji: Char) = getReactions(channelId, id, emoji)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.reactions(vararg emojis: Char): List<Array<User>> = emojis.map { reactions(it) }

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.delete() {
    deleteMessage(channelId, id)
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun deleteMessages(vararg messages: Message) {
    when (messages.size) {
        0 -> return
        1 -> {
            messages[0].delete()
        }
        else -> messages.asSequence()
                .groupBy { it.channelId }
                .forEach { (channelId, group) ->
                    group.chunked(100).forEach {
                        bulkDeleteMessages(channelId, it.toSet())
                    }
                }
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.pin() {
    Bot.pinnedMessages += this
    addPin(channelId, id)
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.unpin() {
    Bot.pinnedMessages -= this
    deletePin(channelId, this)
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.channel(): Channel = channelId.channel()

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Message.guild(): Guild? = guildId?.guild()