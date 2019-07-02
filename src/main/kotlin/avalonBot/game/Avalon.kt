package avalonBot.game

import avalonBot.characters.Character.Loyalty.Good
import avalonBot.characters.LoyalServant
import avalonBot.characters.MinionOfMordred
import avalonBot.players
import avalonBot.roles
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.model.Channel
import lib.util.ping

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class Avalon(val bot: Bot, val gameChannel: Channel) {
    private val gamePlayers: ArrayList<Player> = arrayListOf()

    suspend fun startGame(numEvil: Int): Unit = bot.run {
        val numGood = players.size - numEvil
        val (good, evil) = roles.partition { it.loyalty == Good }.run { first.size to second.size }

        for (i in good until numGood) roles.add(LoyalServant)
        for (i in evil until numEvil) roles.add(MinionOfMordred)

        for ((name, user) in players) {
            val role = roles.random()
            gamePlayers += Player(name, user, role)
            roles -= role
        }

        for (player in gamePlayers) {
            with(player) {
                with(role) {
                    user.sendDM {
                        title = name
                        description = abilitiesDesc
                        color = loyalty.color
                        if (sees.isNotEmpty()) {
                            addField("You can see", sees.joinToString(separator = "\n") { it.name }, inline = true)
                        } else {
                            addField("You see", "no one", inline = true)
                        }
                        val seenPeople = gamePlayers.filter { it.role in sees }
                        if (seenPeople.isNotEmpty()) {
                            addField("You see", seenPeople.joinToString(separator = "\n") {
                                "${it.name} (${it.user.ping()})"
                            }, inline = true)
                        }
                        image(picture)
                    }
                }
            }
        } // notify all players of their role


    }
}