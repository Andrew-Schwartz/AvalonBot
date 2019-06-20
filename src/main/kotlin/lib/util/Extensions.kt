package lib.util

import lib.model.User

operator fun String.get(range: IntRange): String = substring(range)

fun String.underline(): String = "__${this}__"

fun String.italic(): String = "*$this*"

fun String.bold(): String = "**$this**"

fun String.strikethrough(): String = "~~$this~~"

fun String.spoiler(): String = "||$this||"

fun String.inlineCode(): String = "`$this`"

fun String.multilineCode(): String = "```$this```"

fun User.ping(): String = "<@$id>"

//@ExperimentalContracts
//inline val Any?.isNull: Boolean
//    inline get() {
//        contract {
//            returns(false) implies (this@isNull != null)
//        }
//        return this == null
//    }
