package lib.model

interface Storable<T : Storable<T>> {
    val id: Snowflake

    // TODO: most/all of this is not needed???
    fun updateDataFrom(new: T?): T

    val prevVersions: MutableList<T>

    val mostRecent: T?
        get() = prevVersions.lastOrNull()

    @Suppress("UNCHECKED_CAST")
    fun Storable<T>.savePrev(): T {
        // elvis bc prevVersions not initialized by gson
        for (prevVersion in this@Storable.prevVersions ?: mutableListOf()) {
            prevVersions += prevVersion
        }
        prevVersions.add(this@Storable as T)
        return this as T
    }
}