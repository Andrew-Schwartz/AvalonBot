package main.game

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Channel
import lib.model.user.User

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
abstract class Game(val bot: Bot, val channel: Channel) {
    val gamePlayers: ArrayList<Player> = arrayListOf()
    val userPlayerMap: Map<User, Player> by lazy { gamePlayers.associateBy { it.user } }

    abstract suspend fun startGame()
}