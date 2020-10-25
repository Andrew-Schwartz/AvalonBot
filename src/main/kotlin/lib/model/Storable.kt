package lib.model

interface Storable<T : Storable<T>> {
    val id: Snowflake

    infix fun updateFrom(new: T)
}