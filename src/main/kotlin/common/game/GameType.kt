package common.game

import avalon.characters.Character.Loyalty.Evil
import avalon.game.AvalonConfig
import avalon.game.AvalonPlayer
import common.bot
import common.commands.State
import common.util.listGrammatically
import io.ktor.util.KtorExperimentalAPI
import kittens.game.ExplodingKittens
import kittens.game.KittenPlayer
import kittens.game.KittensConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
enum class GameType {
    Avalon {
        override fun player(user: User, guild: Guild?): Player = AvalonPlayer(user, guild)
        override fun game(setup: Setup): Game = avalon.game.Avalon(setup)
        override val config: GameConfig get() = AvalonConfig()
        override val commandState: State = State.Avalon.Game

        override suspend fun startGame(message: Message) {
            bot.run {
                val setup = Setup[message.channel(), Avalon]
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
//                        GlobalScope.launch {
                        val avalon = Game[message.channel(), Avalon] as avalon.game.Avalon
                        avalon.state.numEvil = maxEvil
                        Game.startGame(avalon)
//                        }
                    }
                    else -> message.reply("Error starting Avalon game")
                }
            }
        }
    },
    ExplodingKittens {
        override fun player(user: User, guild: Guild?): Player = KittenPlayer(user, guild)
        override fun game(setup: Setup): Game = ExplodingKittens(setup)
        override val config: GameConfig get() = KittensConfig()
        override val commandState: State = State.Kittens.Game

        override suspend fun startGame(message: Message) {
            TODO("not implemented")
        }
    };

    abstract fun player(user: User, guild: Guild?): Player
    abstract fun game(setup: Setup): Game
    abstract val config: GameConfig
    abstract val commandState: State

    abstract suspend fun startGame(message: Message)

    companion object {
        fun getType(string: String): GameType? = when {
            string.equals("avalon", true) -> Avalon
            "kittens" in string.toLowerCase() -> ExplodingKittens
            else -> null
        }
    }
}