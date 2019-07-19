package avalonBot.characters

import avalonBot.Colors
import avalonBot.characters.Character.Loyalty.Evil
import avalonBot.characters.Character.Loyalty.Good
import lib.model.Color
import java.io.File

val characters: Set<Character> = setOf(
        Assassin,
        LoyalServant,
        Merlin,
        MinionOfMordred,
        Mordred,
        Morgana,
        Oberon,
        Percival
)

abstract class Character {
    abstract val name: String

    abstract val loyalty: Loyalty

    abstract val abilitiesDesc: String

    abstract val sees: Set<Character>

    val picture: File = File("src/main/resources/images/characters/${this::class.simpleName}.jpg")

    enum class Loyalty { Good, Evil }

    val Loyalty.color: Color
        get() = when (this) {
            Good -> Colors.good
            Evil -> Colors.evil
        }
}