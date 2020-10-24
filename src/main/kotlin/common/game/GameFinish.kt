package common.game

import lib.dsl.RichEmbed

data class GameFinish(val message: RichEmbed)/* {
    constructor(embed: suspend RichEmbed.() -> Unit) : this(RichEmbed().apply { embed() })
}*/

//suspend fun gameFinish(embed: suspend RichEmbed.() -> Unit) = lib.dsl.embed(embed)