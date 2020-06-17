package common.game

import lib.dsl.RichEmbed

// TODO some way to return info about this game to make restarting easier
data class GameFinish(
        val message: RichEmbed
) {
    constructor(embed: RichEmbed.() -> Unit) : this(RichEmbed().apply(embed))
}