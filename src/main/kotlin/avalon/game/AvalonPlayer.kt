package avalon.game

import avalon.characters.Character
import common.game.Player
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.guild.Guild
import lib.model.user.User

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class AvalonPlayer(user: User, guild: Guild?) : Player(user, guild) {
    var role: Character? = null

    override fun reset() {
        role = null
    }

    override fun toString() = "Player(name=$name, role=${role?.name})"
}