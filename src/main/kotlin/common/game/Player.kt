package common.game

import common.bot
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.httpRequests.getGuildMember

abstract class Player(user: User, guild: Guild?) : Reset {
    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    val user = with(bot) {
        if (user.member != null || guild == null) user
        else runBlocking { user.copy(member = getGuildMember(guild, user)) }
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    val name: String
        get() = user.member?.nick ?: user.username
}