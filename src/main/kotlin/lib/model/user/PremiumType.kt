package lib.model.user

import com.google.gson.annotations.SerializedName

enum class PremiumType {
    @SerializedName("1")
    NitroClassic,

    @SerializedName("2")
    Nitro,
}