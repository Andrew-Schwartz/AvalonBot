package avalon.game

import avalon.characters.Character
import avalon.characters.LoyalServant
import avalon.characters.MinionOfMordred
import common.game.GameConfig

class AvalonConfig : GameConfig {
    val roles: ArrayList<Character> = arrayListOf()
    var randomRoles: Boolean = false

    var ladyEnabled: Boolean = false

    override fun reset() {
        if (randomRoles) {
            roles.clear()
        } else {
            roles.removeIf { it == LoyalServant || it == MinionOfMordred }
        }
    }

    override fun toString(): String {
        return "AvalonConfig(roles=$roles, randomRoles=$randomRoles, ladyEnabled=$ladyEnabled)"
    }
}