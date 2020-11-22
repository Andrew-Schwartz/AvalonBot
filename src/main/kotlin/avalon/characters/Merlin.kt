package avalon.characters

import avalon.characters.Character.Loyalty.Good
import common.util.S

object Merlin : Character() {
    override val name: String = "Merlin"

    override val loyalty: Loyalty = Good

    override val abilitiesDesc: String = "Sees agents of Evil"

    // TODO should he see Oberon? probably not
    override val sees: Set<Character> = S[Assassin, MinionOfMordred, Morgana]
}