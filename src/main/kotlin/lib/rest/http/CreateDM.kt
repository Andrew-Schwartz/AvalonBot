package lib.rest.http

import com.google.gson.annotations.SerializedName

data class CreateDM(
        @SerializedName("recipient_id") val userId: String // TODO make this a Snowflake?
)