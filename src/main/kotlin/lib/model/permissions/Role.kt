package lib.model.permissions

import lib.model.Color
import lib.model.Snowflake
import lib.model.Storable

data class Role(
        override val id: Snowflake,
        val name: String,
        val color: Color,
        val hoist: Boolean,
        val position: Int,
        val permissions: Int, // bit set
        val managed: Boolean,
        val mentionable: Boolean
) : Storable {
    override fun equals(other: Any?): Boolean = (other as? Role)?.id == id

    override fun hashCode(): Int = id.hashCode()

    override fun addNotNullDataFrom(new: Storable?): Role {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}