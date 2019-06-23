package avalonBot.characters

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
}