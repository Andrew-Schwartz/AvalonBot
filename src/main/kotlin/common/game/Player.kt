package common.game

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import lib.model.guild.Guild
import lib.model.user.User
import lib.rest.http.httpRequests.getGuildMember
import lib.rest.http.httpRequests.getUser

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
abstract class Player(user: User, guild: Guild?) : Reset {
    var user = if (user.member != null || guild == null) {
        user
    } else {
        runBlocking { user.copy(member = getGuildMember(guild, user)) }
    }
        private set

    suspend fun updateUser() {
        user = getUser(user)
    }

    val name: String
        get() = user.member?.nick ?: user.username
}