package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Evil

object MinionOfMordred : Character() {
    override val name: String = "Minion of Mordred"

    override val loyalty: Loyalty = Evil

    override val abilitiesDesc: String = "Sees other minions of Mordred"

    override val sees: Set<Character> = setOf(Assassin, Mordred, Morgana)
}