package lib.util

operator fun String.get(range: IntRange): String = substring(range)

infix fun String.equalsIgnoreCase(other: String): Boolean = this.equals(other, true)