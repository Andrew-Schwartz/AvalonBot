package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Evil
import lib.util.S

object Mordred : Character() {
    override val name: String = "Mordred"

    override val loyalty: Loyalty = Evil

    override val abilitiesDesc: String = """Sees his minions
                                           |Not seen by Merlin""".trimMargin()

    override val sees: Set<Character> = S[Assassin, MinionOfMordred, Morgana]
}