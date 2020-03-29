package common.game

import avalon.characters.Character.Loyalty.Evil
import avalon.game.AvalonConfig
import avalon.game.AvalonPlayer
import common.bot
import common.commands.CommandState.AvalonGame
import common.commands.commandState
import common.util.listGrammatically
import explodingKittens.game.ExplodingKittens
import explodingKittens.game.KittenPlayer
import explodingKittens.game.KittensConfig
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.model.channel.Message
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
enum class GameType {
    Avalon {
        override fun config(): GameConfig = AvalonConfig()
        override fun player(user: User): Player = AvalonPlayer(user)
        override fun game(setup: Setup): Game = avalon.game.Avalon(setup)
        override suspend fun startGame(message: Message) {
            bot.run {
                val setup = Setup[message.channel, Avalon]
                val roles = (setup.config as AvalonConfig).roles
                val maxEvil = when (setup.players.size) {
                    in 5..6 -> 2
                    in 7..9 -> 3
                    10 -> 4
                    else -> -1
                }
                val evil = roles.filter { it.loyalty == Evil }.size
                when {
                    maxEvil == -1 -> message.reply("Between 5 and 10 players are required")
                    roles.size > setup.players.size -> message.reply("You have chosen more roles than there are players")
                    evil > maxEvil -> message.reply("You have too many evil roles: ${roles.filter { it.loyalty == Evil }.listGrammatically()}")
                    evil <= maxEvil -> {
                        message.channel.commandState = AvalonGame
                        GlobalScope.launch {
                            val avalon = Game[message.channel, Avalon] as avalon.game.Avalon
                            avalon.state.numEvil = maxEvil
                            Game.startGame(avalon)
                        }
                        message // exists to make ide not be angry
                    }
                    else -> message.reply("Error starting Avalon game")
                }
            }
        }
    },
    ExplodingKittens {
        override fun config(): GameConfig = KittensConfig()
        override fun player(user: User): Player = KittenPlayer(user)
        override fun game(setup: Setup): Game = ExplodingKittens(setup)
        override suspend fun startGame(message: Message) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    };

    abstract fun config(): GameConfig
    abstract fun player(user: User): Player
    abstract fun game(setup: Setup): Game

    abstract suspend fun startGame(message: Message)

    companion object {
        fun getType(string: String): GameType? = when {
            string.equals("avalon", true) -> Avalon
            "kittens" in string.toLowerCase() -> ExplodingKittens
            else -> null
        }
    }
}