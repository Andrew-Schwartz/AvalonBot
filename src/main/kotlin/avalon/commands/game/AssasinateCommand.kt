package avalon.commands.game

import avalon.characters.Assassin
import avalon.characters.Character
import avalon.game.AvalonPlayer
import avalon.game.AvalonState
import common.commands.MessageCommand
import common.commands.State
import common.util.L
import common.util.onNull
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.reply
import lib.model.channel.Message

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class AssassinateCommand(state: AvalonState, set: (AvalonPlayer) -> Unit) : MessageCommand(State.Avalon.Assassinate) {
    override val execute: suspend (Message) -> Unit = { message ->
        with(state) {
            if (userPlayerMap[message.author]?.role != Assassin) return@with
            if (message.mentions.size != 1) {
                message.reply(", assassinate one player (@ them)", ping = true)
                return@with
            }
            userPlayerMap[message.mentions.single()]?.let { target ->
                if (target.role?.loyalty == Character.Loyalty.Good) {
                    set(target)
                } else {
                    message.reply(", you can only assassinate a Good player", ping = true)
                }
            }.onNull { message.reply(", assassinate a player in the game", ping = true) }
        }
    }
    override val name: String = "assassinate"
    override val aliases: List<(name: String) -> Boolean> = L[{ it.startsWith("ass") }]
    override val description: String = "Only usable by the assassin. Assassinate your best guess of Merlin"
    override val usage: String = "assassinate <user>"
}