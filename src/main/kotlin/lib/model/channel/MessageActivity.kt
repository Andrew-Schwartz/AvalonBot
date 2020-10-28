package lib.model.channel

import com.google.gson.annotations.SerializedName

data class MessageActivity(
        val type: MessageActivityType,
        @SerializedName("party_id") val partyId: String,
)

enum class MessageActivityType {
    @SerializedName("1")
    Join,

    @SerializedName("2")
    Spectate,

    @SerializedName("3")
    Listen,

    @SerializedName("5")
    JoinRequest
}