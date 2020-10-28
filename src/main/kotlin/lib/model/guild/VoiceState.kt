package lib.model.guild

import com.google.gson.annotations.SerializedName
import lib.model.ChannelId
import lib.model.GuildId
import lib.model.UserId

data class VoiceState(
        @SerializedName("guild_id") val guildId: GuildId?,
        @SerializedName("channel_id") val channelId: ChannelId?,
        @SerializedName("user_id") val userId: UserId,
        val member: GuildMember?,
        val session_id: String,
        val deaf: Boolean,
        val mute: Boolean,
        @SerializedName("self_deaf") val selfDeaf: Boolean,
        @SerializedName("self_mute") val selfMute: Boolean,
        val suppress: Boolean,
)
