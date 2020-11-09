package lib.util

import lib.model.channel.Channel
import lib.model.emoji.Emoji
import lib.model.permissions.Role
import lib.model.user.User

fun String.underline(): String = "__${this}__"

fun String.italic(): String = "*$this*"

fun String.bold(): String = "**$this**"

fun String.strikethrough(): String = "~~$this~~"

fun String.spoiler(): String = "||$this||"

fun String.inlineCode(): String = "`$this`"

fun String.multilineCode(): String = "```\n$this\n```"

// todo ??
fun User.pingReal(): String = "<@$id>"

// todo ??
fun User.ping(): String = "<@!$id>"

fun Channel.link(): String = "<#$id>"

fun Role.link(): String = "<@&$id>"

fun Emoji.show(): String = "<:$name:$id>"

fun Emoji.showAnimated(): String = "a${show()}"