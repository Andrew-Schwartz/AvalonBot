package lib.model

inline class Snowflake(val value: String) {
    override fun toString(): String = value
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.snowflake(): Snowflake = Snowflake(this)