package lib.model

interface Storable<T : Storable<T>> {
    val id: Snowflake

    fun updateDataFrom(new: T?): T

    val prevVersions: MutableList<T>

    val mostRecent: T?
        get() = prevVersions.lastOrNull()

    @Suppress("UNCHECKED_CAST")
    fun Storable<T>.savePrev(): T {
        for (prevVersion in this@Storable.prevVersions
                ?: mutableListOf()) { // elvis bc prevVersions not initialized by gson
            prevVersions += prevVersion
        }
        prevVersions.add(this@Storable as T)
        return this as T
    }
}