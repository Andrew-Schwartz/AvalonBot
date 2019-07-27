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
) : Storable