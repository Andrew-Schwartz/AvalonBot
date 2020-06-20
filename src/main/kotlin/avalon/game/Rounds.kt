package avalon.game

import common.util.L
import common.util.M

class Rounds(private val numPlayers: Int) {
    operator fun get(roundNum: Int): Round = map.getValue(numPlayers)[roundNum - 1]
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