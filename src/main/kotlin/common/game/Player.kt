package common.game

import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.getMember
import lib.model.guild.Guild
import lib.model.guild.GuildMember
import lib.model.user.User
import lib.rest.http.httpRequests.getUser

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
abstract class Player(var user: User, val guild: Guild?) : Reset {
    var member: GuildMember? = null

    suspend fun updateUser() {
        user = getUser(user)
        if (guild != null) {
            member = user.getMember(guild)
        }
    }

    //    suspend fun name(): String {
//        return if (guild == null) {
//            user.username
//        } else {
//            if (member == null) {
//                member = user.getMember(guild)
//            }
//            member?.nick ?: user.username
//        }
//    }
    val name: String
        get() = member?.nick ?: user.username
}