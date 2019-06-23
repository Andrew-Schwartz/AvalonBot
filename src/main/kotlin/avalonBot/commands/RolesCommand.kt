package avalonBot.commands

import avalonBot.characters.LoyalServant
import avalonBot.characters.MinionOfMordred
import avalonBot.characters.characters
import avalonBot.neutral
import avalonBot.roles
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Message

object RolesCommand : Command() {
    override val name: String = "roles"

    override val description: String = """
        |Pick which roles will be available in the next game of Avalon, not including Loyal Servants or Minions of Mordred.
        |Roles are Assassin, Merlin, Mordred, Morgana, Oberon, and Percival
        """.trimMargin()

    override val usage: String = "!roles [reset] [list] [role1] [role2] [role3] etc..."

    @KtorExperimentalAPI
    @ExperimentalCoroutinesApi
    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
        if ("reset" in args) roles.clear()
        if ("list" in args) message.reply {
            title = "Remaining roles"
            description = (characters - roles - setOf(LoyalServant, MinionOfMordred)).joinToString(separator = "\n") { it.name }
        }

        roles += args.filter { it !in arrayOf("reset", "list") }.mapNotNull { name ->
            try {
                characters.first { name == it.name }
            } catch (e: NoSuchElementException) {
                message.reply("No role by the name $name")
                null
            }
        }

        message.reply {
            color = neutral
            title = "Current Roles"
            description = if (roles.isEmpty()) "none" else roles.joinToString(separator = "\n") { it.name }
        }
    }
}