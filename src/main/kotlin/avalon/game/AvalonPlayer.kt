package avalon.game

import avalon.characters.Character
import common.game.Player
import common.game.name
import lib.model.guild.Guild
import lib.model.user.User

class AvalonPlayer(user: User, guild: Guild?) : Player(user, guild) {
    var role: Character? = null

    override fun toString() = "Player(name=$name, role=${role?.name})"
}