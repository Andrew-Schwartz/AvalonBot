package common.game

import lib.dsl.RichEmbed

data class GameFinish(val message: RichEmbed) {
    constructor(embed: RichEmbed.() -> Unit) : this(RichEmbed().apply(embed))
}