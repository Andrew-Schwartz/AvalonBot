package avalonBot.game

import avalonBot.characters.Character
import lib.model.user.User

class Player(val name: String, val user: User, val role: Character) {
    val username = user.username

    val nameAndUser = "$name ($username)"

    override fun toString(): String = "Player(name=$name, user=${user.username}, role=${role.name})"
}