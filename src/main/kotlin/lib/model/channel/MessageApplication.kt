package lib.model.channel

import com.google.gson.annotations.SerializedName
import lib.model.ApplicationId

data class MessageApplication(
        val id: ApplicationId,
        @SerializedName("cover_image") val coverImage: String,
        val description: String,
        val icon: String?,
        val name: String,
)