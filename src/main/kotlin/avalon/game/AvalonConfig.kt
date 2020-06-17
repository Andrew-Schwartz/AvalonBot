package avalon.game

import avalon.characters.Character
import common.game.GameConfig

class AvalonConfig : GameConfig {
    val roles: ArrayList<Character> = arrayListOf()
    var randomRoles: Boolean = false

    var ladyEnabled: Boolean = false

    override fun toString(): String {
        return "AvalonConfig(roles=$roles, ladyEnabled=$ladyEnabled)"
    }
}