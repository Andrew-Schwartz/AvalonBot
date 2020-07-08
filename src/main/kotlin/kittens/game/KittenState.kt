package kittens.game

import common.game.Game
import common.game.GameType
import common.game.Setup
import common.game.State
import common.util.replaceCamelCase
import io.ktor.util.KtorExperimentalAPI
import kittens.cards.Card
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class KittenState(setup: Setup) : State<KittenPlayer>(setup) {
    private val config = setup.config as KittenConfig

    var currentPlayerIndex = 0

    var imploding = config.implodingKittens

    //    override val players: List<KittenPlayer> = setup.players.map { it as KittenPlayer } as ArrayList<KittenPlayer>
    val currentPlayer
        get() = players[currentPlayerIndex]
    val nextPlayer
        get() = players[next(currentPlayerIndex)]
    var turnOrder = 1

    val deck = arrayListOf<Card>()
    val allCards = arrayListOf<Card>()

    fun next(index: Int): Int {
        var index = index
        index += turnOrder
        index %= players.size
        if (index < 0)
            index += players.size
        return index
    }

    fun List<String>.cards(): List<Card> {
        return (1..4).flatMap { i ->
            windowed(i)
                    .map { it.joinToString(separator = "") }
                    .mapNotNull { cardName ->
                        allCards.firstOrNull {
                            it::class.simpleName!!.replaceCamelCase(" ").equals(cardName, true)
                        }
                    }
        }
    }

    companion object {
        fun inChannel(channel: Channel) = Game[channel, GameType.Kittens]?.state as? KittenState
    }
}
