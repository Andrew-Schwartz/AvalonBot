package explodingKittens

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Channel
import main.game.Game

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class ExplodingKittens(bot: Bot, channel: Channel) : Game(bot, channel) {
    val state: KittenState = KittenState(this)

    override suspend fun startGame(): Unit = bot.run {

    }
}