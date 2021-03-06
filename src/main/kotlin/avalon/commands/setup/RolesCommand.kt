package avalon.commands.setup

import avalon.characters.LoyalServant
import avalon.characters.MinionOfMordred
import avalon.characters.characters
import avalon.game.AvalonConfig
import common.commands.MessageCommand
import common.commands.State
import common.game.GameType
import common.game.Setup
import common.util.A
import common.util.S
import common.util.onNull
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.channel
import lib.dsl.reply
import lib.dsl.send
import lib.model.Color
import lib.model.Color.Companion.gold
import lib.model.channel.Message

object RolesCommand : MessageCommand(State.Setup.Setup) {
    private const val CLEAR_ROLES = "reset"
    private const val LIST_ROLES = "list"
    private const val RANDOM_ROLES = "random"

    override val name: String = "roles"

    override val description: String = """
        |Pick which roles will be available in the next game of Avalon, not including Loyal Servants or Minions of Mordred.
        |Roles are Assassin, Merlin, Mordred, Morgana, Oberon, and Percival
        """.trimMargin()

    override val usage: String = "roles [$CLEAR_ROLES] [$LIST_ROLES] [$RANDOM_ROLES [role1] [role2] [role3] etc..."

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend (Message) -> Unit = { message ->
        val setup = Setup[message.channel(), GameType.Avalon]
        val roles = (setup.config as AvalonConfig).roles
        val args = message.args

        if (CLEAR_ROLES in args) roles.clear()

        args.filter { it !in A[CLEAR_ROLES, LIST_ROLES, RANDOM_ROLES] }
                .mapNotNull { name ->
                    characters.firstOrNull { name.equals(it.name, ignoreCase = true) }
                            .onNull { message.reply("No role by the name $name") }
                }
                .forEach {
                    if (it in roles) {
                        roles -= it
                    } else {
                        roles += it
                    }
                }

        if (LIST_ROLES in args && roles.isEmpty()) {
            message.channel().send {
                title = "Remaining roles"
                description = (characters - roles - S[LoyalServant, MinionOfMordred]).joinToString(separator = "\n") { it.name }
            }
        } else if (RANDOM_ROLES in args && roles.isEmpty()) {
            setup.config.randomRoles = !setup.config.randomRoles
            message.channel().send {
                title = "Random mode ${if (setup.config.randomRoles) "engaged!" else "disengaged"}"
                color = gold
            }
        } else {
            message.channel().send {
                color = Color.gold
                title = "Current Roles"
                description = if (roles.isEmpty()) "none" else roles.joinToString(separator = "\n") { it.name }
            }
        }
    }
}