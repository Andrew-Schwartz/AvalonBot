package lib.model

//TODO make different snowflakes for each type
inline class Snowflake(val value: String) {
    override fun toString(): String = value
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.snowflake(): Snowflake = Snowflake(this)