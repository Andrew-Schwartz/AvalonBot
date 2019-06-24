package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Evil
import lib.util.S

object MinionOfMordred : Character() {
    override val name: String = "Minion of Mordred"

    override val loyalty: Loyalty = Evil

    override val abilitiesDesc: String = "Sees other minions of Mordred"

    override val sees: Set<Character> = S[Assassin, Mordred, Morgana]
}