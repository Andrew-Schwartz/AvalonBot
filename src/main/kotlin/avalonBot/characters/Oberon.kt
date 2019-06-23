package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Evil

object Oberon : Character() {
    override val name: String = "Oberon"

    override val loyalty: Loyalty = Evil

    override val abilitiesDesc: String = "Neither seen by minions of Mordred nor by Merlin"

    override val sees: Set<Character> = setOf()
}