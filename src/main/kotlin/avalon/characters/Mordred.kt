package avalon.characters

import avalon.characters.Character.Loyalty.Evil
import main.util.S

object Mordred : Character() {
    override val name: String = "Mordred"

    override val loyalty: Loyalty = Evil

    override val abilitiesDesc: String = """Sees his minions
                                           |Not seen by Merlin""".trimMargin()

    override val sees: Set<Character> = S[Assassin, MinionOfMordred, Morgana]
}