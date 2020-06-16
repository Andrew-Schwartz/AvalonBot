package common.commands

import common.game.Game
import common.game.GameFinish
import common.game.GameType
import common.game.Setup
import common.steadfast
import common.util.getOrDefault
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Color.Companion.gold
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin

object RestartCommand : MessageCommand(State.Game) {
    override val name: String = "restart"

    override val description: String = "Restart a game if all players agree. You must specify which game to start (Avalon or Exploding Kittens)"

    override val usage: String = "restart <game> ['now']"

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message) -> Unit = { message ->
        with(message) {
            val gameFromGames = GameType.values()
                    .map { it to Game[message.channel(), it] }
                    .singleOrNull { it.second.started }
                    ?.first
            val gameFromArgs = GameType.getType(args.getOrDefault(0, ""))

            val gameType = gameFromArgs
                    ?: gameFromGames
                    ?: message.reply(" Specify which game to start", ping = true).let { return@with }

            val game = Game[message.channel(), gameType]
            if (args.lastOrNull()?.equals("now", true) == true) {
                if (author != steadfast) {
                    message.reply("Only Andrew is that cool")
                } else {
                    Game.endAndRemove(message.channel(), gameType, GameFinish {
                        title = "Manually restarted"
                        color = gold
                    })
                    message.channel().states += State.Setup.Setup
                    Setup.remove(message.channel(), gameType)
                    for (pin in pinnedMessages) {
                        runCatching { deletePin(pin.channelId, pin) }
                                .onFailure { println(it.message) }
                    }
                }
            } else {
                val approveChar = '✔'
                val rejectChar = '❌'
                val botMsg = message.reply("React ✔ if you are ready to start the game, if you're not ready react ❌")
                botMsg.react(approveChar)
                botMsg.react(rejectChar)
                // TODO all of this
//                GlobalScope.launch {
//                suspendUntil {
//                    val (approves, rejects) = botMsg.
//                }
//                }
            }
        }
    }
}