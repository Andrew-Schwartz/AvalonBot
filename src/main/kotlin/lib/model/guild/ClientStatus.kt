package lib.model.guild

import lib.rest.model.events.sendEvents.Status

data class ClientStatus(
        val desktop: Status?,
        val mobile: Status?,
        val web: Status?,
)

//enum class Status {
//    @SerializedName("desktop")
//    Desktop,
//    @SerializedName("mobile")
//    Mobile,
//    @SerializedName("web")
//    Web
//}