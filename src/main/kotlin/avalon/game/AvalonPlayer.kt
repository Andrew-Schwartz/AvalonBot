package avalon.game

import avalon.characters.Character
import common.game.Player
import lib.model.user.User

class AvalonPlayer(user: User) : Player(user) {
    var role: Character? = null

    override fun toString() = "Player(name=${user.username}, role=${role?.name})"
}