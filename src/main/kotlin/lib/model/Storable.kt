package lib.model

interface Storable<I : Snowflake, T : IntoId<I>> : IntoId<I> {
    infix fun updateFrom(new: T)
}