package lib.model.channel

import com.google.gson.annotations.SerializedName

enum class ChannelType {
    @SerializedName("0")
    GuildText,
    @SerializedName("1")
    DM,
    @SerializedName("2")
    GuildVoice,
    @SerializedName("3")
    GroupDM,
    @SerializedName("4")
    GuildCategory,
    @SerializedName("5")
    GuildNews,
    @SerializedName("6")
    GuildStore;

    val isText: Boolean
        get() = this == GuildText || this == DM || this == GroupDM

    val isVoice: Boolean
        get() = this == GuildVoice
}
