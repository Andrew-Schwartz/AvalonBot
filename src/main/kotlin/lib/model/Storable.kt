package lib.model

interface Storable {
    val id: Snowflake

    fun updateDataFrom(new: Storable?): Storable
}