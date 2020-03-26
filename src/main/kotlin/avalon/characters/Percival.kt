package avalon.characters

import avalon.characters.Character.Loyalty.Good
import common.util.S

object Percival : Character() {
    override val name: String = "Percival"

    override val loyalty: Loyalty = Good

    override val abilitiesDesc: String = "Can see Merlin, but may think that Morgana is Merlin"

    override val sees: Set<Character> = S[Merlin, Morgana]
}
