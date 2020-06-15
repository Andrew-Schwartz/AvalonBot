package avalon.game

import avalon.characters.Character
import common.game.GameConfig

class AvalonConfig : GameConfig {
    val roles: ArrayList<Character> = arrayListOf()

    var ladyEnabled: Boolean = false

    override fun toString(): String {
        return "AvalonConfig(roles=$roles, ladyEnabled=$ladyEnabled)"
    }
}