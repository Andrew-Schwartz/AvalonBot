package lib.model.channel

import com.google.gson.annotations.SerializedName

enum class MessageType {
    @SerializedName("0")
    Default,
    @SerializedName("1")
    RecipientAdd,
    @SerializedName("2")
    RecipientRemove,
    @SerializedName("3")
    Call,
    @SerializedName("4")
    ChannelNameChange,
    @SerializedName("5")
    ChannelIconChange,
    @SerializedName("6")
    ChannelPinnedChange,
    @SerializedName("7")
    GuildMemberJoin,
    @SerializedName("8")
    UserPremiumGuildSubscription,
    @SerializedName("9")
    UserPremiumGuildSubscriptionTier1,
    @SerializedName("10")
    UserPremiumGuildSubscriptionTier2,
    @SerializedName("11")
    UserPremiumGuildSubscriptionTier3
}
