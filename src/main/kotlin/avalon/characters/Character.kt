package avalon.characters

import avalon.characters.Character.Loyalty.Evil
import avalon.characters.Character.Loyalty.Good
import common.util.S
import lib.model.Color
import java.io.File

val characters: Set<Character> = S[
        Assassin,
        LoyalServant,
        Merlin,
        MinionOfMordred,
        Mordred,
        Morgana,
        Oberon,
        Percival
]

abstract class Character {
    abstract val name: String

    abstract val loyalty: Loyalty

    abstract val abilitiesDesc: String

    abstract val sees: Set<Character>

    val picture: File = File("src/main/resources/images/avalon/characters/${this::class.simpleName}.jpg")

    enum class Loyalty {
        Good {
            override val image: File = File("src/main/resources/images/avalon/loyaltyGood.jpg")
        },
        Evil {
            override val image: File = File("src/main/resources/images/avalon/loyaltyBad.jpg")
        };

        abstract val image: File
    }

    val Loyalty.color: Color
        get() = when (this) {
            Good -> Color.blue
            Evil -> Color.red
        }
}