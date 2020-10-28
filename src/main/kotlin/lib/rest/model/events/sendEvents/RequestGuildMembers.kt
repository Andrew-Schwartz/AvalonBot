package lib.rest.model.events.sendEvents

import com.google.gson.annotations.SerializedName
import lib.model.GuildId
import lib.model.IntoId
import lib.model.UserId
import lib.rest.model.GatewayOpcode
import lib.rest.model.events.receiveEvents.Intent.Intents
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_MEMBERS
import lib.rest.model.events.receiveEvents.Intent.Intents.GUILD_PRESENCES

class RequestGuildMembers private constructor(
        @SerializedName("guild_id") val guildId: GuildId,
        val query: String?,
        val limit: Int?,
        @SerializedName("user_ids") val userIds: List<UserId>,
        val presences: Boolean,
        val nonce: String?,
) : SendEvent {
    override val opcode: GatewayOpcode = GatewayOpcode.RequestGuildMembers

    init {
        if (userIds.isEmpty() && query == null) throw IllegalStateException("Must set one of `query` or `userIds`")

        if (presences) Intents += GUILD_PRESENCES
        if (query == "" && limit == 0) Intents += GUILD_MEMBERS
    }

    companion object {
        fun query(guildId: IntoId<GuildId>, query: String, limit: Int = 0, presences: Boolean = false, nonce: String? = null) =
                RequestGuildMembers(guildId.intoId(), query, limit, emptyList(), presences, nonce)

        fun users(guildId: IntoId<GuildId>, users: List<IntoId<UserId>>, presences: Boolean = false, nonce: String? = null) =
                RequestGuildMembers(guildId.intoId(), null, null, users.map { it.intoId() }, presences, nonce)
    }
}