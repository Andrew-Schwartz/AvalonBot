package avalon.game

import avalon.characters.Character
import common.game.Player
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.guild.Guild
import lib.model.user.User

class AvalonPlayer(user: User, guild: Guild?) : Player(user, guild) {
    var role: Character? = null

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override fun toString() = "Player(name=$name, role=${role?.name})"
}