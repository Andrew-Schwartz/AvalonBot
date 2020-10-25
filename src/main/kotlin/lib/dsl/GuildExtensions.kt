package lib.dsl

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.IntoId
import lib.model.UserId
import lib.model.guild.Guild
import lib.model.guild.GuildMember
import lib.rest.http.httpRequests.getGuildMember

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
suspend fun Guild.getMember(id: IntoId<UserId>): GuildMember = getGuildMember(this, id)