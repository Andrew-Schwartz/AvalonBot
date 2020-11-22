package lib.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun now(): String = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd HH:mm:ss"))

@Suppress("NOTHING_TO_INLINE")
inline fun log(msg: String) = println("[${now()}] $msg")

