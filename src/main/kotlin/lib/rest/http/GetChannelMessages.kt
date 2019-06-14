package lib.rest.http

import lib.model.Snowflake

class GetChannelMessages private constructor(
        val channel: Snowflake,
        val limit: Int,
        val around: Snowflake? = null,
        val before: Snowflake? = null,
        val after: Snowflake? = null
) {
    val queryParams: String
        get() = when {
            around != null -> "around=$around"
            before != null -> "before=$before"
            after != null -> "after=$after"
            else -> throw IllegalStateException("None of around, before, or after were not null (somehow?)")
        } + "&limit=$limit"

    companion object {
        fun around(channel: Snowflake, messageId: Snowflake, limit: Int = 50): GetChannelMessages {
            return GetChannelMessages(channel, limit, around = messageId)
        }

        fun before(channel: Snowflake, messageId: Snowflake, limit: Int = 50): GetChannelMessages {
            return GetChannelMessages(channel, limit, before = messageId)
        }

        fun after(channel: Snowflake, messageId: Snowflake, limit: Int = 50): GetChannelMessages {
            return GetChannelMessages(channel, limit, after = messageId)
        }
    }
}