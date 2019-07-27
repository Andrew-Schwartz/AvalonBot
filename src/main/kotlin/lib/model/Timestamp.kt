package lib.model

import java.time.OffsetDateTime

inline class Timestamp(val time: String) {
    val dateTime: OffsetDateTime
        get() = OffsetDateTime.parse(time)
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.timestamp() = Timestamp(this)

@Suppress("NOTHING_TO_INLINE")
inline fun OffsetDateTime.timestamp() = Timestamp(toString())