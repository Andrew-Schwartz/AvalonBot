package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Evil

object Assassin : Character() {
    override val name: String = ""

    override val loyalty: Loyalty = Evil

    override val abilitiesDesc: String = """Attempts to assassinate Merlin at end of game
                                           |Sees other minions of Mordred""".trimMargin()

    override val sees: Set<Character> = setOf(MinionOfMordred, Mordred, Morgana)
}