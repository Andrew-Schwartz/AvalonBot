package lib.model.guild

import lib.model.Snowflake

data class Account(
        val id: Snowflake, // TODO maybe should be just string
        val name: String
)
