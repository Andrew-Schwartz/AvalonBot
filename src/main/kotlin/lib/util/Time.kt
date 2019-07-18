package lib.util

import lib.model.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

fun parseRfc1123(timestamp: String): Long = RFC_1123_DATE_TIME.parse(timestamp, Instant::from).epochSecond

fun parseRfc1123(timestamp: Timestamp): Long = parseRfc1123(timestamp.time)