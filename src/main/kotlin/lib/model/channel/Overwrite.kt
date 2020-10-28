package lib.model.channel

import lib.model.UserRoleId

data class Overwrite(
        val id: UserRoleId,
        val type: String,
        val allow: Int, // bit set
        val deny: Int, // bit set
)