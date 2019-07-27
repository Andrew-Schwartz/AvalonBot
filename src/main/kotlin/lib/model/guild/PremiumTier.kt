package lib.model.guild

import com.google.gson.annotations.SerializedName

enum class PremiumTier {
    @SerializedName("0")
    None,
    @SerializedName("1")
    Tier1,
    @SerializedName("2")
    Tier2,
    @SerializedName("3")
    Tier3,
}
