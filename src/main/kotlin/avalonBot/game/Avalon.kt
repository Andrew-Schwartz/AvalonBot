package avalonBot.game

import avalonBot.Colors
import avalonBot.Colors.good
import avalonBot.Colors.neutral
import avalonBot.characters.Assassin
import avalonBot.characters.Character.Loyalty.Evil
import avalonBot.characters.Character.Loyalty.Good
import avalonBot.characters.LoyalServant
import avalonBot.characters.Merlin
import avalonBot.characters.MinionOfMordred
import avalonBot.players
import avalonBot.roles
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.*
import lib.model.channel.Channel
import lib.model.channel.Message
import lib.model.user.User
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.util.*

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class Avalon(val bot: Bot, val gameChannel: Channel) {
    private val gamePlayers: ArrayList<Player> = arrayListOf()
    private val userPlayerMap: Map<User, Player> by lazy { gamePlayers.associateBy { it.user } }
    private var party: Set<Player>? = null
    private lateinit var rounds: Rounds

    private var roundNum = 1
    private var leaderNum = 0
    private var rejectedQuests = 0
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

        if (roles.isNotEmpty()) println("BIG PROBLEM")
        roles += gamePlayers.map { it.role }

        on(MessageCreate, infoCommand)
        on(MessageCreate, questCommand)

        for (player in gamePlayers) {
            println("Telling ${player.username} their role")
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
                }.pin()
            }
        } // notify all players of their role
        gamePlayers.shuffle()

        GlobalScope.launch {
            gameLoop@ while (goodWins < 3 && evilWins < 3) {
                val round = rounds[roundNum]

                gameChannel.send(pingTargets = A[leader.user]) {
                    color = neutral
                    title = "The leader is ${leader.name}".underline()
//                    description = leader.user.ping()
                    addField(
                            "Use !quest to choose people to send on the quest (separate names with ${";".inlineCode()})",
                            "Send ${round.players} people on this quest." +
                                    if (round.fails != 1) "2 failures are needed for this quest to fail." else ""
                    )
//                    image(leaderCrown)
                }
                party = null


                blockUntil { party != null } // turn listener
                gameChannel.send {
                    title = "${leader.name} has chosen that ${formatIterable(party!!) { it.name }} will go on this quest"
                    description = "react to my DM to Approve or Reject this party"
                }
                val approveChar = '✔'
                val rejectChar = '❌'
                val messages: ArrayList<Message> = arrayListOf()
                for (player in gamePlayers) {
                    val msg = player.user.sendDM("React ✔ to vote to approve the quest, or ❌ to reject it\n" +
                            "The proposed party is ${formatIterable(party!!) { it.name }}")
                    messages += msg
                    msg.react(approveChar)
                    msg.react(rejectChar)
                }


                blockUntil {
                    messages.all { (it.reactions(approveChar).size == 2) xor (it.reactions(rejectChar).size == 2) }
                }
                val (approve, reject) = messages.partition { it.reactions(approveChar).size == 2 }.map { it.size }
                if (reject >= approve) {
                    leaderNum++
                    rejectedQuests++
                    if (rejectedQuests == 5) {
                        gameChannel.send {
                            color = Colors.evil
                            title = "There have been 5 rejected parties in a row so the bad guys win"
                        }
                        break@gameLoop
                    } else {
                        gameChannel.send("There are now $rejectedQuests rejects in a row")
                    }
                    continue@gameLoop
                }

                gameChannel.send {
                    color = neutral
                    title = "The party has been accepted!"
                    description = "Now ${formatIterable(party!!) { it.username }} will either succeed or fail the quest"
                    for (msg in messages) {
                        val reactors = msg.reactions(approveChar)
                        addField(userPlayerMap[msg.channel.recipients?.first()]?.name
                                ?: "Lol it's null", if (reactors.size == 2) "Approved" else "Rejected", inline = true)
                    }
                }
                messages.clear()
                for (player in party!!) {
                    val msg = player.user.sendDM("React ✔ to succeed the quest" + if (player.role.loyalty == Evil) ", or ❌ to fail it" else "")
                    messages += msg
                    msg.react(approveChar)
                    if (player.role.loyalty == Evil)
                        msg.react(rejectChar)
                }


                blockUntil {
                    messages.all { msg ->
                        msg.reactions(approveChar).size == 2 || msg.reactions(rejectChar).size == 2
                    }
                }
                val (successes: Int, fails: Int) = messages.partition { it.reactions(approveChar).size == 2 }.map { it.size }

                if (fails >= round.fails) {
                    evilWins++
                    gameChannel.send {
                        color = Colors.evil
                        title = "There " + if (fails == 1) "was 1 fail" else "were $fails fails"
                        description = "Reminder: ${formatIterable(party!!) { it.name }} were on this quest"
                    }.pin()
                } else {
                    goodWins++
                    gameChannel.send {
                        color = Colors.good
                        title = if (fails == 0) "All $successes were successes" else "There was $fails fail, but ${round.fails} are required this round"
                        description = "Reminder: ${formatIterable(party!!) { it.name }} were on this quest"
                    }.pin()
                }

                when (3) {
                    goodWins -> {
                        if (Assassin in roles && Merlin in roles) {
                            gameChannel.send {
                                title = "The good guys have succeeded three quests, but the Assassin can try to kill Merlin"
                                description = "Assassin, use ${"!assassinate <name>".inlineCode()} to assassinate who you think Merlin is"
                                gamePlayers.filter { it.role.loyalty == Evil }.forEach { addField(it.name.underline(), it.role.name, inline = true) }
                            }
                            var merlinGuess: Player? = null
                            val assassinateListener: suspend Message.() -> Unit = {
                                if (userPlayerMap[author]?.role == Assassin && content.startsWith("!ass")) {
                                    val name = args[0]
                                    merlinGuess = gamePlayers.firstOrNull { it.name == name || it.username == name }
                                }
                            }
                            on(MessageCreate, assassinateListener)

                            launch {
                                blockUntil { merlinGuess != null }
                                off(MessageCreate, assassinateListener)

                                if (merlinGuess!!.role == Merlin) {
                                    gameChannel.send {
                                        color = Colors.evil
                                        title = "Correct! ${merlinGuess!!.name} was Merlin! The bad guys win!"
                                        revealAllRoles()
                                    }
                                } else {
                                    gameChannel.send {
                                        color = Colors.good
                                        title = "Incorrect! The good guys win!"
                                        revealAllRoles()
                                    }
                                }
                            }
                        } else {
                            gameChannel.send {
                                color = Colors.good
                                title = "The good guys win!"
                                gamePlayers.forEach { addField(it.name.underline(), it.role.name, inline = true) }
                            }
                        }
                    }
                    evilWins -> gameChannel.send {
                        color = Colors.evil
                        title = "The bad guys win!"
                        gamePlayers.forEach { addField(it.name.underline(), it.role.name, inline = true) }
                    }
                }

                roundNum++
                leaderNum++
            }

            cleanupListeners()
        }
    }

    private fun RichEmbed.revealAllRoles() {
        gamePlayers.forEach { addField("${it.name} (${it.username})".underline(), it.role.name, inline = true) }
    }

    private fun <T> formatIterable(iterable: Iterable<T>, toString: (T) -> String): String {
        return iterable.map(toString)
                .reduceIndexed { index, acc, name ->
                    "$acc${
                    if (index == iterable.count() - 1) " and"
                    else ","
                    } $name"
                }
    }

    fun cleanupListeners() = bot.run {
        off(MessageCreate, infoCommand)
        off(MessageCreate, questCommand)
    }

    private val infoCommand: suspend Message.() -> Unit = {
        bot.run {
            if (author.isBot != true && content == "!info") {
                reply {
                    title = "Avalon Info".underline()
                    color = when {
                        goodWins > evilWins -> good
                        goodWins == evilWins -> neutral
                        else -> Colors.evil
                    }
                    description = "Here's where there would be an edited pic of the Avalon board if I was cool"
                    addField("Number of Good Victories".underline(), "$goodWins", true)
                    addField("Number of Evil Victories".underline(), "$evilWins", true)
                    addField("Current Leader".underline(), gamePlayers[rounds[roundNum].players].user.ping(), true)
                    addField("Round Number".underline(), "$roundNum", true)
//                    addField("Order of leaders".underline(), gamePlayers.joinToString(separator = ", "), true)
//                    addField("Number of rejected party proposals", rounds[roundNum].fails.toString(), true) that's how many is required not happened lol
                }
            }
        }
    }

    private val questCommand: suspend Message.() -> Unit = {
        bot.run {
            if (author != leader.user) return@run
            if (!content.startsWith("!quest")) return@run

            val round = rounds[roundNum]
            val questers = content.substring(content.indexOf(' ') + 1).split(" *; *".toRegex()).mapNotNull { arg ->
                try {
                    gamePlayers.first { it.name.equals(arg, ignoreCase = true) || it.username.equals(arg, ignoreCase = true) }
                } catch (e: NoSuchElementException) {
                    reply("No one by the (nick)name $arg")
                    null
                }
            }.toSet()

            if (questers.size != round.players) {
                reply(ping = true, content = "\nYou need to send ${round.players} people on the quest!")
                return@run
            }
            party = questers
        }
    }
}
