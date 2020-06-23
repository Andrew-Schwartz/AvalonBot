package lib.model.permissions

import lib.model.Color
import lib.model.IntoId
import lib.model.RoleId
import lib.model.Storable

data class Role(
        override val id: RoleId,
        val name: String,
        val color: Color,
        val hoist: Boolean,
        val position: Int,
        val permissions: Int, // bit set
        val managed: Boolean,
        val mentionable: Boolean
) : Storable<Role>, IntoId<RoleId> by id {
    @Suppress("USELESS_ELVIS")
    override fun updateDataFrom(new: Role?): Role {
        val r = new ?: return this

        return Role(
                r.id ?: id,
                r.name ?: name,
                r.color ?: color,
                r.hoist ?: hoist,
                r.position ?: position,
                r.permissions ?: permissions,
                r.managed ?: managed,
                r.mentionable ?: mentionable
        ).savePrev()
    }

    override val prevVersions: MutableList<Role> = mutableListOf()

    override fun equals(other: Any?): Boolean = (other as? Role)?.id == id

    override fun hashCode(): Int = id.hashCode()
}