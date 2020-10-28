package lib.rest.http

import lib.model.ChannelId
import lib.model.IntoId
import lib.model.MessageId

class GetChannelMessages private constructor(
        val channel: ChannelId,
        val limit: Int,
        val around: MessageId? = null,
        val before: MessageId? = null,
        val after: MessageId? = null,
) {
    val queryParams: String
        get() = when {
            around != null -> "around=$around"
            before != null -> "before=$before"
            after != null -> "after=$after"
            else -> throw IllegalStateException("None of around, before, or after were not null (somehow?)")
        } + "&limit=$limit"

    companion object {
        fun around(channel: IntoId<ChannelId>, messageId: IntoId<MessageId>, limit: Int = 50): GetChannelMessages {
            return GetChannelMessages(channel.intoId(), limit.coerceIn(1..100), around = messageId.intoId())
        }

        fun before(channel: IntoId<ChannelId>, messageId: IntoId<MessageId>, limit: Int = 50): GetChannelMessages {
            return GetChannelMessages(channel.intoId(), limit.coerceIn(1..100), before = messageId.intoId())
        }

        fun after(channel: IntoId<ChannelId>, messageId: IntoId<MessageId>, limit: Int = 50): GetChannelMessages {
            return GetChannelMessages(channel.intoId(), limit.coerceIn(1..100), after = messageId.intoId())
        }
    }
}