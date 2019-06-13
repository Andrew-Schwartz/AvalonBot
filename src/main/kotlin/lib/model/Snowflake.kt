package lib.model

inline class Snowflake(val value: String) {
    override fun toString(): String = value
}