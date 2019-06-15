package lib.misc

operator fun String.get(range: IntRange): String = substring(range)