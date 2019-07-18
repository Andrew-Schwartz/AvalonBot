package avalonBot.game

import avalonBot.*
import avalonBot.characters.Character.Loyalty.Good
import avalonBot.characters.LoyalServant
import avalonBot.characters.MinionOfMordred
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import lib.dsl.Bot
import lib.dsl.off
import lib.dsl.on
import lib.model.Channel
import lib.model.Message
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.util.inlineCode
import lib.util.ping
import lib.util.pingReal
import lib.util.underline

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class Avalon(val bot: Bot, val gameChannel: Channel) {
    private val gamePlayers: ArrayList<Player> = arrayListOf()
    private var party: Set<Player>? = null
    private lateinit var rounds: Rounds

    private var roundNum = 1
    private var leaderNum = 0
    private var goodWins = 0
    private var evilWins = 0

    val leader get() = gamePlayers[leaderNum % gamePlayers.size]

    suspend fun startGame(numEvil: Int): Unit = bot.run {
        rounds = Rounds(players.size)
        val numGood = players.size - numEvil
        val (good, evil) = roles.partition { it.loyalty == Good }.run { first.size to second.size }

        for (i in good until numGood) roles.add(LoyalServant)
        for (i in evil until numEvil) roles.add(MinionOfMordred)

        for ((name, user) in players) {
            val role = roles.random()
            gamePlayers += Player(name, user, role)
            roles -= role
        }

        on(MessageCreate, infoCommand)

        for (player in gamePlayers) {
            with(player.role) {
                player.user.sendDM {
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
                        addField("You see",
                                seenPeople
                                        .filter { it.name != player.name }
                                        .joinToString(separator = "\n") { "${it.name} (${it.user.pingReal()})" },
                                inline = true)
                    }
                    image(picture)
                }
            }
        } // notify all players of their role
        gamePlayers.shuffle()

        Turn(bot).run()
    }

    private val infoCommand: suspend Message.() -> Unit = {
        bot.run {
            if (author.isBot != true && content == "!info") {
                reply {
                    title = "Avalon Info".underline()
                    color = when {
                        goodWins > evilWins -> good
                        goodWins == evilWins -> neutral
                        else -> evil
                    }
                    description = "Here's where there would be an edited pic of the Avalon board if I was cool"
                    addField("Number of Good Victories".underline(), "$goodWins", true)
                    addField("Number of Evil Victories".underline(), "$evilWins", true)
                    addField("Current Leader".underline(), gamePlayers[rounds[roundNum].players].user.ping(), true)
                    addField("Round Number".underline(), "$roundNum", true)
                    addField("Order of leaders".underline(), gamePlayers.joinToString(separator = ", "), true)
//                    addField("Number of rejected party proposals", rounds[roundNum].fails.toString(), true) that's how many is required not happened lol
                }
            }
        }
    }

    inner class Turn(private val bot: Bot, private val prevTurn: Turn? = null) {
        suspend fun run(): Unit = bot.run {
            val round = rounds[roundNum]

            gameChannel.send {
                color = neutral
                title = "The leader is ${leader.name}".underline()
                description = leader.user.ping()
                addField(
                        "Use !quest to choose people to send on the quest (separate names with ${";".inlineCode()})",
                        "Send ${round.players} people on this quest." +
                                if (round.fails != 1) "2 failures are needed for this quest to fail." else ""
                )
//                image(leaderCrown)
            }

            prevTurn?.run {
                println("removed a listener")
                off(MessageCreate, prevTurn!!.turnListener)
            }
            on(MessageCreate, turnListener)
        }

        private val turnListener: suspend Message.() -> Unit = {
            bot.run {
                val round = rounds[roundNum]
                // have to remove this listener
                val leader = gamePlayers[roundNum]
                if (author != leader.user) return@run
                if (!content.startsWith("!quest")) return@run

                val questers = content.substring(content.indexOf(' ') + 1).split(" *; *".toRegex()).mapNotNull { arg ->
                    try {
                        gamePlayers.first { it.name == arg || it.username == arg }
                    } catch (e: NoSuchElementException) {
                        reply("No one by the (nick)name $arg")
                        null
                    }
                }.toSet()

                when (questers.size) {
                    round.players -> {
                        party = questers
                        reply {
                            title = "${leader.user.pingReal()} has chosen that ${party!!
                                    .map(Player::name)
                                    .reduceIndexed { index, acc, name ->
                                        "$acc${
                                        if (index == party!!.size - 1) " and"
                                        else ","
                                        } $name"
                                    }
                            } will go on this quest"
                            description = "react to my DM to Approve or Reject this party"
                        }
                        for (player in gamePlayers) {
                            val dm = player.user.getDM()
                            val msg = dm.send("React ✔ to vote to approve the quest, or ❌ to reject it")
                            msg.react('✔')
                            msg.react('❌')
                        }
                    }
                    else -> {
                        reply(ping = true, content = "\nYou need to send ${round.players} people on the quest!")
                        return@run
                    }
                }

                println("next round!! ($roundNum)")
                //...
                roundNum++
                leaderNum++
                // first remove this listener
                Turn(bot, this@Turn).run()
            }
        }

    }
}