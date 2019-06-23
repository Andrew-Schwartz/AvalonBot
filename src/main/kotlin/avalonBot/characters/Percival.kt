package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Good

object Percival : Character() {
    override val name: String = "Percival"

    override val loyalty: Loyalty = Good

    override val abilitiesDesc: String = "Can see Merlin, but may think that Morgana is Merlin"

    override val sees: Set<Character> = setOf(Merlin, Morgana)
}
