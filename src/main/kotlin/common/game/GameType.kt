package common.game

import avalon.game.AvalonConfig
import avalon.game.AvalonPlayer
import explodingKittens.KittenPlayer
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
enum class GameType {
    Avalon {
        override fun data(): GameConfig = AvalonConfig()
        override fun player(user: User): Player = AvalonPlayer(user)
        override fun game(setup: Setup): Game = avalon.game.Avalon(setup)
    },
    ExplodingKittens {
        override fun data(): GameConfig = TODO("KittenData")
        override fun player(user: User): Player = KittenPlayer(user)
        override fun game(setup: Setup): Game = explodingKittens.ExplodingKittens(setup)
    };

    abstract fun data(): GameConfig
    abstract fun player(user: User): Player
    abstract fun game(setup: Setup): Game

//    @KtorExperimentalAPI
//    @ExperimentalCoroutinesApi
//    fun getPlayer(user: User): Player = when (this) {
//        Avalon -> AvalonPlayer(user)
//        ExplodingKittens -> KittenPlayer(user)
//    }

//    @KtorExperimentalAPI
//    @ExperimentalCoroutinesApi
//    fun getGame(setup: Setup): Game = when (this) {
//        Avalon -> avalon.game.Avalon(setup)
//        ExplodingKittens -> explodingKittens.ExplodingKittens(setup)
//    }
}
