package lib.model

interface Storable {
    val id: Snowflake

    fun addNotNullDataFrom(new: Storable?): Storable
}