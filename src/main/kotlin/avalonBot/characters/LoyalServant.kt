package avalonBot.characters

import avalonBot.characters.Character.Loyalty.Good

object LoyalServant : Character() {
    override val name: String = "Loyal Servant"

    override val loyalty: Loyalty = Good

    override val abilitiesDesc: String = "None"

    override val sees: Set<Character> = emptySet()
}