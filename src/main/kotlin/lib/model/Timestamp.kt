package lib.model

import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

inline class Timestamp(val time: String) : Comparable<Timestamp> {
    val dateTime: OffsetDateTime
        get() = OffsetDateTime.parse(time)

    override fun compareTo(other: Timestamp): Int = time.compareTo(other.time)
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.timestamp() = Timestamp(this)

@Suppress("NOTHING_TO_INLINE")
inline fun OffsetDateTime.timestamp() = Timestamp(toString())

fun parseRfc1123(timestamp: String): Long = DateTimeFormatter.RFC_1123_DATE_TIME.parse(timestamp, Instant::from).epochSecond

fun parseRfc1123(timestamp: Timestamp): Long = parseRfc1123(timestamp.time)