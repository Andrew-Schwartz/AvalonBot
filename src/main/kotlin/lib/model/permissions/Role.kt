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
    @Suppress("USELESS_ELVIS")
    override fun updateDataFrom(new: Storable?): Role {
        val r = (new as? Role) ?: throw IllegalArgumentException("Can only copy info from other role")

        return Role(
                r.id ?: id,
                r.name ?: name,
                r.color ?: color,
                r.hoist ?: hoist,
                r.position ?: position,
                r.permissions ?: permissions,
                r.managed ?: managed,
                r.mentionable ?: mentionable
        )
    }

    override fun equals(other: Any?): Boolean = (other as? Role)?.id == id

    override fun hashCode(): Int = id.hashCode()
}