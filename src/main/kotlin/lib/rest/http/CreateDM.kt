package lib.rest.http

import com.google.gson.annotations.SerializedName
import lib.model.Snowflake

data class CreateDM(
        @SerializedName("recipient_id") val userId: Snowflake
)