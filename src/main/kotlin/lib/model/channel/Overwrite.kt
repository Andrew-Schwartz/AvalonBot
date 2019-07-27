package lib.model.channel

import lib.model.Snowflake

data class Overwrite(
        val id: Snowflake,
        val type: String,
        val allow: Int, // bit set
        val deny: Int // bit set
)