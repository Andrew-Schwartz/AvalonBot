package lib.rest.http

import com.google.gson.annotations.SerializedName

data class CreateDM(
        @SerializedName("recipient_id") val recipientId: String
)