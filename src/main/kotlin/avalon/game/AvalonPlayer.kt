package avalon.game

import avalon.characters.Character
import lib.model.user.User
import main.game.Player

class AvalonPlayer(user: User) : Player(user) {
    var role: Character? = null

    override fun toString() = "Player(name=${user.username}, role=${role?.name})"
}