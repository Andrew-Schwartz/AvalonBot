package lib.rest.http

import com.google.gson.annotations.SerializedName
import lib.model.ChannelId
import lib.model.channel.Channel
import lib.model.channel.Overwrite

@Suppress("ArrayInDataClass")
data class ModifyChannelOptions(
        val name: String? = null,
        val position: Int? = null,
        val topic: String? = null,
        val nsfw: Boolean? = null,
        @SerializedName("rate_limit_per_user") val rateLimitPerUser: Int? = null,
        val bitrate: Int? = null,
        @SerializedName("userLimit") val userLimit: Int? = null,
        @SerializedName("permission_overwrites") val permissionOverwrites: Array<Overwrite>? = null,
        @SerializedName("parent_id") val parentId: ChannelId? = null,
) {
    infix fun forChannel(channel: Channel): ModifyChannelOptions = when {
        channel.isText -> copy(bitrate = null, userLimit = null)
        channel.isVoice -> copy(topic = null, nsfw = null, rateLimitPerUser = null)
        else -> {
            println("Channel ${channel.name} is not voice or text, it's type is ${channel.type}")
            copy(topic = null, nsfw = null, rateLimitPerUser = null, bitrate = null, userLimit = null)
        }
    }
}