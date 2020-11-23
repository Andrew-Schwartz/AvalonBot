package lib.model.permissions

import lib.model.Color
import lib.model.IntoId
import lib.model.RoleId
import lib.model.Storable

data class Role(
        val id: RoleId,
        var name: String,
        var color: Color,
        var hoist: Boolean,
        var position: Int,
        var permissions: Int, // bit set
        var managed: Boolean,
        var mentionable: Boolean,
) : Storable<RoleId, Role>, IntoId<RoleId> by id {
    @Suppress("USELESS_ELVIS")
    override fun updateFrom(new: Role) {
        name = new.name ?: name
        color = new.color ?: color
        hoist = new.hoist ?: hoist
        position = new.position ?: position
        permissions = new.permissions ?: permissions
        managed = new.managed ?: managed
        mentionable = new.mentionable ?: mentionable
    }

    override fun equals(other: Any?): Boolean = (other as? Role)?.id == id

    override fun hashCode(): Int = id.hashCode()
}