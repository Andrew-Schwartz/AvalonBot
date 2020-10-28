package lib.rest.http

data class EditChannelPermissions internal constructor(
        val allow: Int,
        val deny: Int,
        val type: String,
) {
    companion object {
        fun forUser(allow: Int, deny: Int): EditChannelPermissions {
            return EditChannelPermissions(allow, deny, "member")
        }

        fun forRole(allow: Int, deny: Int): EditChannelPermissions {
            return EditChannelPermissions(allow, deny, "role")
        }
    }
}