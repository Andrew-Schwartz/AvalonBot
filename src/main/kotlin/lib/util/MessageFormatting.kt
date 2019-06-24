package lib.util

import lib.model.Channel
import lib.model.Emoji
import lib.model.Role
import lib.model.User

fun String.underline(): String = "__${this}__"

fun String.italic(): String = "*$this*"

fun String.bold(): String = "**$this**"

fun String.strikethrough(): String = "~~$this~~"

fun String.spoiler(): String = "||$this||"

fun String.inlineCode(): String = "`$this`"

fun String.multilineCode(): String = "```$this```"

fun User.ping(): String = "<@$id>"

fun User.pingNick(): String = "<@!$id>"

fun Channel.link(): String = "<#$id>"

fun Role.link(): String = "<@&$id>"

fun Emoji.show(): String = "<:$name:$id>"

fun Emoji.showAnimated(): String = "a${show()}"