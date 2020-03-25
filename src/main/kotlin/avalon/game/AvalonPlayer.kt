package avalon.game

import avalon.characters.Character
import lib.model.user.User
import main.game.Player

class AvalonPlayer(name: String, user: User, val role: Character) : Player(name, user) {
    override fun toString() = "Player(name=$name, user=${user.username}, role=${role.name})"
}