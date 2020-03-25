package main

import lib.model.Snowflake

data class ConfigJson(
        val token: String,
        val prefix: String,
        val steadfastId: Snowflake,
        val ktsId: Snowflake
)