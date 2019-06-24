package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Evil
import lib.util.S

object Morgana : Character() {
    override val name: String = "Morgana"

    override val loyalty: Loyalty = Evil

    override val abilitiesDesc: String = """Appears as Merlin to Percival
                                           |Sees other minions of Mordred""".trimMargin()

    override val sees: Set<Character> = S[Assassin, MinionOfMordred, Mordred]
}