package avalonBot.game

import avalonBot.characters.Character
import lib.model.User

class Player(val name: String, val user: User, val role: Character) {
    override fun toString(): String = "Player(name=$name, user=${user.username}, role=${role.name})"
}