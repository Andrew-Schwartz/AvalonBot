package avalon.game

import common.util.L
import common.util.M

class Rounds(private val numPlayers: Int) {
    operator fun get(roundNum: Int): Round = map.getValue(numPlayers)[roundNum - 1]

    override fun toString(): String {
        val list = map[numPlayers] ?: return ""
        return list.zip((1..5)).joinToString(separator = "\n") { (round, i) ->
            buildString {
                append(i)
                when (i) {
                    1 -> append("st")
                    2 -> append("nd")
                    3 -> append("rd")
                    4, 5 -> append("th")
                }
                append(" Round: ${round.players} on the quest.")
                if (round.fails != 1)
                    append("${round.fails} fails are required to fail it.")
            }
        }
    }
}

// @formatter:off
private val map: Map<Int, List<Round>> = M[
        5  to L[r(2), r(3), r(2), r(3),    r(3)],
        6  to L[r(2), r(3), r(4), r(3),    r(4)],
        7  to L[r(2), r(3), r(3), r(4, 2), r(4)],
        8  to L[r(3), r(4), r(4), r(5, 2), r(5)],
        9  to L[r(3), r(4), r(4), r(5, 2), r(5)],
        10 to L[r(3), r(4), r(4), r(5, 2), r(5)]
]
// @formatter:on

data class Round(val players: Int, val fails: Int = 1)

@Suppress("NOTHING_TO_INLINE")
private inline fun r(numPlayers: Int, numFailsRequired: Int = 1) = Round(numPlayers, numFailsRequired)