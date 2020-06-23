package avalon.characters

import common.util.S

// TODO get a picture
object Jester : Character() {
    override val name: String = "Jester"

    override val loyalty: Loyalty = Loyalty.Good

    override val abilitiesDesc: String = "Only wins if they are assassinated at the end of the game. Sees the assassin"

    override val sees: Set<Character> = S[Assassin]
}