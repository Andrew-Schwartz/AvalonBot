package avalon.characters

import avalon.characters.Character.Loyalty.Good
import common.util.S

object Merlin : Character() {
    override val name: String = "Merlin"

    override val loyalty: Loyalty = Good

    override val abilitiesDesc: String = "Sees agents of Evil"

    override val sees: Set<Character> = S[Assassin, MinionOfMordred, Morgana]
}