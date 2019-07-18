package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Evil
import lib.util.S

object Assassin : Character() {
    override val name: String = "Assassin"

    override val loyalty: Loyalty = Evil

    override val abilitiesDesc: String = """Attempts to assassinate Merlin at end of game
                                           |Sees other minions of Mordred""".trimMargin()

    override val sees: Set<Character> = S[MinionOfMordred, Mordred, Morgana]
}