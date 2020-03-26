package main.game

import avalon.game.AvalonPlayer
import explodingKittens.KittenPlayer
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Channel
import lib.model.user.User

enum class GameType {
    Avalon,
    ExplodingKittens;

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    fun getPlayer(user: User): Player = when (this) {
        Avalon -> AvalonPlayer(user)
        ExplodingKittens -> KittenPlayer(user)
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    fun getGame(setup: Setup): Game = when (this) {
        Avalon -> avalon.game.Avalon(setup)
        ExplodingKittens -> explodingKittens.ExplodingKittens(setup)
    }

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    fun getGame(channel: Channel): Game = getGame(Setup[channel, this])
}
