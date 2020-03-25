package avalon.commands.setup

import avalon.Colors
import avalon.characters.LoyalServant
import avalon.characters.MinionOfMordred
import avalon.characters.characters
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.channel.Message
import main.commands.Command
import main.commands.CommandState.Setup
import main.roles
import main.util.A
import main.util.S
import main.util.onNull

object RolesCommand : Command(Setup) {
    private const val CLEAR_ROLES = "reset"
    private const val LIST_ROLES = "list"

    override val name: String = "roles"

    override val description: String = """
        |Pick which roles will be available in the next game of Avalon, not including Loyal Servants or Minions of Mordred.
        |Roles are Assassin, Merlin, Mordred, Morgana, Oberon, and Percival
        """.trimMargin()

    override val usage: String = "!roles [$CLEAR_ROLES] [$LIST_ROLES] [role1] [role2] [role3] etc..."

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        if (CLEAR_ROLES in args) roles.clear()

        args.filter { it !in A[CLEAR_ROLES, LIST_ROLES] }
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
            message.reply {
                title = "Remaining roles"
                description = (characters - roles - S[LoyalServant, MinionOfMordred]).joinToString(separator = "\n") { it.name }
            }
        } else {
            message.reply {
                color = Colors.neutral
                title = "Current Roles"
                description = if (roles.isEmpty()) "none" else roles.joinToString(separator = "\n") { it.name }
            }
        }
    }
}