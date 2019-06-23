package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Good

object Merlin : Character() {
    override val name: String = "Merlin"

    override val loyalty: Loyalty = Good

    override val abilitiesDesc: String = "Sees agents of Evil"

    override val sees: Set<Character> = setOf(Assassin, MinionOfMordred, Morgana)
}