package lib.model.guild

import com.google.gson.annotations.SerializedName

data class ClientStatus(
        val desktop: Status?,
        val mobile: Status,
        val web: Status?
)

enum class Status {
    @SerializedName("desktop")
    Desktop,
    @SerializedName("mobile")
    Mobile,
    @SerializedName("web")
    Web
}