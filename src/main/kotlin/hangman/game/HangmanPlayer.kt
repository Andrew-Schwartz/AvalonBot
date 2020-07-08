package hangman.game

import common.game.Player
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.model.guild.Guild
import lib.model.user.User

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class HangmanPlayer(user: User, guild: Guild?) : Player(user, guild) {
    override fun reset() {}
}