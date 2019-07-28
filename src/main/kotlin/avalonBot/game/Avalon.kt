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

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Avalon(val bot: Bot, val gameChannel: Channel) {
    private val gamePlayers: ArrayList<Player> = arrayListOf()
    private val userPlayerMap: Map<User, Player> by lazy { gamePlayers.associateBy { it.user } }
    private lateinit var rounds: Rounds

    private var party: Set<Player>? = null
    private var ladyTarget: Player? = null
        set(value) {
            value?.let { pastLadies += it }
            field = value
        }
    private val pastLadies: MutableList<Player> = mutableListOf()

    private var roundNum = 1
    private var leaderNum = 0
    private var goodWins = 0
    private var rejectedQuests = 0
    private var evilWins = 0
    private var ladyOfTheLake: Player? = null

    val leader get() = gamePlayers[leaderNum % gamePlayers.size]

    suspend fun startGame(numEvil: Int, ladyEnabled: Boolean): Unit = bot.run {
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
        gamePlayers.shuffle()
        if (ladyEnabled) {
            ladyOfTheLake = gamePlayers.last()
        }

        if (roles.isNotEmpty()) println("BIG PROBLEM")
        roles += gamePlayers.map { it.role }

        on(MessageCreate, infoCommand)

        gameChannel.startTyping()
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

        GlobalScope.launch {
            gameLoop@ while (goodWins < 3 && evilWins < 3) {
                val round = rounds[roundNum]

                on(MessageCreate, questCommand)
                gameChannel.send(pingTargets = A[leader.user]) {
                    color = neutral
                    title = "The leader is ${leader.name}".underline()
                    if (ladyEnabled && roundNum != 5)
                        description = "${ladyOfTheLake!!.nameAndUser} has the Lady of the Lake"
                    addField(
                            "Use !quest to choose people to send on the quest (separate names with ${";".inlineCode()})",
                            "Send ${round.players} people on this quest." +
                                    if (round.fails != 1) " 2 failures are needed for this quest to fail." else ""
                    )
                }
                party = null


                blockUntil { party != null } // turn listener
                off(MessageCreate, questCommand)
                gameChannel.send {
                    title = "${leader.name} has chosen that ${formatIterable(party!!) { it.name }} will go on this quest"
                    description = "react to my DM to Approve or Reject this party"
                }
                gamePlayers.forEach { it.user.getDM().startTyping() }
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
                println("All players can now on the party")


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
                        gameChannel.send {
                            title = "There are now $rejectedQuests rejects in a row"
                            for (msg in messages) {
                                val reactors = msg.reactions(approveChar)
                                addField(userPlayerMap[msg.channel.recipients?.first()]?.name
                                        ?: "Lol it's null", if (reactors.size == 2) "Approved" else "Rejected", inline = true)
                            }
                        }
                    }
                    continue@gameLoop
                }

                gameChannel.send {
                    color = neutral
                    title = "The party has been accepted!"
                    description = "Now ${formatIterable(party!!) { it.nameAndUser }} will either succeed or fail the quest"
                    for (msg in messages) {
                        val reactors = msg.reactions(approveChar)
                        addField(userPlayerMap[msg.channel.recipients?.first()]?.name
                                ?: "Lol it's null", if (reactors.size == 2) "Approved" else "Rejected", inline = true)
                    }
                }
                messages.clear()
                party!!.forEach { it.user.getDM().startTyping() }
                for (player in party!!) {
                    val msg = player.user.sendDM("React ✔ to succeed the quest" + if (player.role.loyalty == Evil) ", or ❌ to fail it" else "")
                    messages += msg
                    msg.react(approveChar)
                    if (player.role.loyalty == Evil)
                        msg.react(rejectChar)
                }
                println("Everyone can now succeed/fail the quest")


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

                            GlobalScope.launch {
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

                if (ladyEnabled && roundNum in 2..4 && goodWins < 3 && evilWins < 3) {
                    on(MessageCreate, ladyCommand)

                    gameChannel.send {
                        title = "Now ${ladyOfTheLake!!.name} will use the Lady of the Lake on someone to find their alignment"
                        description = "use ${"!lady".inlineCode()} and a player's name/username"
                    }
                    blockUntil { ladyTarget != null }
                    off(MessageCreate, ladyCommand)

                    ladyOfTheLake!!.user.sendDM {
                        title = "${ladyTarget!!.name} is ${ladyTarget!!.role.loyalty}"
                        image(ladyTarget!!.role.loyalty.image)
                    }

                    ladyOfTheLake = ladyTarget
                    ladyTarget = null
                }

                roundNum++
                leaderNum++
            }

            cleanupListeners()
        }
    }

    private fun RichEmbed.revealAllRoles() {
        gamePlayers.forEach { addField(it.nameAndUser.underline(), it.role.name, inline = true) }
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

    private fun cleanupListeners() = bot.run {
        off(MessageCreate, infoCommand)
        off(MessageCreate, questCommand)
    }

    private val infoCommand: suspend Message.() -> Unit = {
        bot.run {
            if (author.isBot != true && content.startsWith("!info", ignoreCase = true)) {
                reply {
                    title = "Avalon Info".underline()
                    color = when {
                        goodWins > evilWins -> good
                        goodWins == evilWins -> neutral
                        else -> Colors.evil
                    }
//                    description = "Here's where there would be an edited pic of the Avalon board if I was cool"
                    description = "Order of leaders\n".underline() + gamePlayers.joinToString(separator = "\n") { it.nameAndUser }
                    addField("Number of Good Victories".underline(), "$goodWins", true)
                    addField("Number of Evil Victories".underline(), "$evilWins", true)
                    addField("Current Leader".underline(), gamePlayers[rounds[roundNum].players].user.ping(), true)
                    addField("Round Number".underline(), "$roundNum", true)
//                    addField("Number of rejected party proposals", rounds[roundNum].fails.toString(), true) that's how many is required not happened lol
                }
            }
        }
    }

    private val questCommand: suspend Message.() -> Unit = {
        bot.run {
            if (author != leader.user) return@run
            if (!content.startsWith("!quest", ignoreCase = true)) return@run

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

    private val ladyCommand: suspend Message.() -> Unit = {
        bot.run {
            if (ladyOfTheLake?.user != author) return@run
            if (!content.startsWith("!lady", ignoreCase = true)) return@run

            val name = content.substringAfter(' ')

            when (val potentialTarget = gamePlayers.firstOrNull { it.name.equals(name, ignoreCase = true) || it.username.equals(name, ignoreCase = true) }) {
                null -> reply("No user found for the name $name")
                ladyOfTheLake -> reply("You cannot use the Lady of the Lake to determine your own role")
                in pastLadies -> reply("$name has already had the Lady of the Lake")
                else -> ladyTarget = potentialTarget
            }
        }
    }
}
