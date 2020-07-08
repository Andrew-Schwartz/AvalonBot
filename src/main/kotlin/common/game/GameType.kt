package common.game

import avalon.characters.Character.Loyalty.Evil
import avalon.game.AvalonConfig
import avalon.game.AvalonPlayer
import common.bot
import common.commands.State
import common.util.listGrammatically
import hangman.game.HangmanConfig
import hangman.game.HangmanPlayer
import io.ktor.util.KtorExperimentalAPI
import kittens.game.ExplodingKittens
import kittens.game.KittenConfig
import kittens.game.KittenPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.channel.Message
import lib.model.guild.Guild
import lib.model.user.User
import kotlin.reflect.KClass
import avalon.game.Avalon as AvalonGame
import hangman.game.Hangman as HangmanGame

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
enum class GameType {
    Avalon {
        override fun player(user: User, guild: Guild?): Player = AvalonPlayer(user, guild)
        override fun game(setup: Setup): Game = AvalonGame(setup)
        override val config: GameConfig get() = AvalonConfig()
        override val states: StateInfo = StateInfo(State.Avalon.Game, State.Setup.AvalonStart, State.Avalon::class)

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
                        val avalon = Game(message.channel(), Avalon) as avalon.game.Avalon
                        avalon.state.numEvil = maxEvil
                        Game.runGame(avalon)
                    }
                    else -> message.reply("Error starting Avalon game")
                }
            }
        }
    },
    Kittens {
        override fun player(user: User, guild: Guild?): Player = KittenPlayer(user, guild)
        override fun game(setup: Setup): Game = ExplodingKittens(setup)
        override val config: GameConfig get() = KittenConfig()
        override val states: StateInfo = StateInfo(State.Kittens.Game, State.Setup.KittensStart, State.Kittens::class)

        override suspend fun startGame(message: Message) {
            TODO("not implemented")
        }
    },
    Hangman {
        override fun player(user: User, guild: Guild?): Player = HangmanPlayer(user, guild)
        override fun game(setup: Setup): Game = hangman.game.Hangman(setup)
        override val config: GameConfig = HangmanConfig()
        override val states: StateInfo = StateInfo(State.Hangman.Game, State.Setup.Setup, State.Hangman::class)

        override suspend fun startGame(message: Message) {
            bot.run {
                val hangman = Game(message.channel(), Hangman) as HangmanGame
                Game.runGame(hangman)
            }
        }
    };

    abstract fun player(user: User, guild: Guild?): Player
    abstract fun game(setup: Setup): Game
    abstract val config: GameConfig
    abstract val states: StateInfo

    abstract suspend fun startGame(message: Message)

    data class StateInfo(val commandState: State, val startVotingState: State, val parentClass: KClass<out State>)

    companion object {
        fun getType(string: String): GameType? = when {
            string.equals("avalon", true) -> Avalon
            "kittens" in string.toLowerCase() -> Kittens
            else -> null
        }
    }
}