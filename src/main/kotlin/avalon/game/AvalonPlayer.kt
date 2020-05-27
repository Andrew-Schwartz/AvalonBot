package avalon.game

import avalon.characters.Character
import common.game.Player
import common.game.name
import lib.model.user.User

class AvalonPlayer(user: User) : Player(user) {
    var role: Character? = null

    override fun toString() = "Player(name=$name, role=${role?.name})"
}