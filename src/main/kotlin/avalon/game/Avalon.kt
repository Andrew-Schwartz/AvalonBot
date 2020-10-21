package avalon.game

import avalon.characters.*
import avalon.characters.Character.Loyalty.Evil
import avalon.characters.Character.Loyalty.Good
import avalon.commands.game.VoteCommand
import common.commands.State
import common.commands.State.Avalon.Voting
import common.commands.states
import common.game.*
import common.util.*
import io.ktor.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import lib.dsl.*
import lib.model.Color
import lib.model.Color.Companion.gold
import lib.model.Color.Companion.red
import lib.model.channel.Message
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.rest.model.events.receiveEvents.MessageUpdate
import lib.util.inlineCode
import lib.util.ping
import lib.util.underline
import kotlin.math.absoluteValue

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Avalon(setup: Setup) : Game(GameType.Avalon, setup) {
    override val state: AvalonState = AvalonState(setup)

    override suspend fun runGame(): GameFinish = with(state) game@{
        channel.states -= State.Setup.Setup
        rounds = Rounds(players.size)
        val numGood = players.size - state.numEvil

        if (randomRoles) {
            if (roles.isNotEmpty()) {
                return@game GameFinish {
                    title = "Random roles were on but these roles were also selected manually"
                    description = roles.joinToString(separator = "\n") { it.name }
                    color = red
                }
            }
            // TODO these numbers
            val good = ML[Merlin, Percival]
            repeat(if (players.size == 5) 4 else 6) { good.add(LoyalServant) }
            val evil = ML[Assassin, Mordred, Morgana, Oberon]
            repeat(numEvil * 2) { evil.add(MinionOfMordred) }
            good.shuffle()
            evil.shuffle()
            while (good.size > numGood) good.removeAt(0)
            while (evil.size > numEvil) evil.removeAt(0)
            roles.addAll(good)
            roles.addAll(evil)
        } else {
            val (good, evil) = roles.partition { it.loyalty == Good }.map { it.size }
            for (i in good until numGood) roles.add(LoyalServant)
            for (i in evil until numEvil) roles.add(MinionOfMordred)
        }

        for (player in players) {
            roles.random().let {
                player.role = it
                roles -= it
            }
        }
        if (roles.isNotEmpty()) {
            println("roles = $roles")
            println("players = $players")
            throw RuntimeException("ROLES WERE NOT EMPTY")
        }
        roles += players.map { it.role!! }

        if (ladyEnabled) {
            ladyOfTheLake = players.last()
        }

        channel.startTyping()
        for (player in players) {
            println("Telling ${player.name} their role")
            with(player.role!!) {
                player.user.sendDM {
                    title = name
                    description = abilitiesDesc
                    color = loyalty.color
                    val seesString = sees.joinToString(separator = "\n") { it.name }
                            .takeIf { it.isNotEmpty() }
                            ?: "no one"
                    addField("You can see", seesString, true)
                    val seenPeople = players.filter { it.role in sees }
                    if (seenPeople.isNotEmpty()) {
                        addField("You see",
                                seenPeople
                                        .filter { channel.debug || it.name != player.name }
                                        .joinToString(separator = "\n") {
                                            val ping = it.user.ping()
                                            if (ping.isEmpty()) it.name else ping
                                        },
                                inline = true)
                    }
                    image(picture)
                }.apply {
                    pin()
                    this@Avalon.pinnedMessages += this
                }
            }
        } // notify all players of their role

        channel.send {
            title = "Avalon game with ${players.size} players"
            color = gold
            addField("The roles are",
                    if (randomRoles) "random" else roles.sortedBy { it.loyalty }.joinToString(separator = "\n") { it.name },
                    inline = true)
            addField("Lady of the Lake", if (ladyEnabled) "is enabled" else "is disabled", inline = true)
            addField("Round Breakdown", rounds.toString())
        }

        gameLoop@ while (goodWins < 3 && evilWins < 3) {
            val round = rounds[roundNum]

            channel.states += State.Avalon.Quest
            channel.send(pingTargets = A[leader.user]) {
                color = gold
                title = "The leader is ${leader.name}".underline()
                if (ladyEnabled && roundNum != 5)
                    description = "${ladyOfTheLake!!.user.username} has the Lady of the Lake"
                addField(
                        "Use ${"!quest".inlineCode()} to choose people to send on the quest (ping the people you are sending)",
                        "Send ${round.players} people on this quest." +
                                if (round.fails != 1) " 2 failures are needed for this quest to fail." else ""
                )
            }
            party = null

            suspendUntil { party != null } // turn listener
            channel.states -= State.Avalon.Quest
            channel.send {
                title = "${leader.name} has chosen that ${party?.listGrammatically { it.name }} will go on this quest"
                description = "react to my DM to Approve or Reject this party"
            }
            players.forEach { it.user.getDM().startTyping() }
            val approveChar = VoteCommand.approveChar
            val rejectChar = VoteCommand.rejectChar
            reacts.clear()
            for (player in players) {
                val msg = player.user.sendDM("React ✔ to vote to approve the quest, or ❌ to reject it\n" +
                        "The proposed party is ${party?.listGrammatically { it.name }}")
                reacts[msg] = 0
                Bot.launch {
                    msg.react(approveChar)
                    msg.react(rejectChar)
                }
            }
            channel.states += Voting
            println("All players can now vote on the party")

            suspendUntil(50) {
                reacts.values.all { it.absoluteValue == 1 }
            }
            channel.states -= Voting
            val (approve, reject) = reacts.values.partition { it == 1 }.map { it.size }
            if (reject >= approve) {
                leaderNum++
                rejectedQuests++
                if (rejectedQuests == 5) {
                    return@game GameFinish {
                        color = red
                        title = "There have been 5 rejected parties in a row so the bad guys win"
                    }
                } else {
                    channel.send {
                        title = when (rejectedQuests) {
                            1 -> "There is now 1 reject"
                            else -> "There are now $rejectedQuests rejects in a row"
                        }
                        for (msg in reacts.keys) {
                            val reactors = msg.reactions(approveChar)
                            addField(userPlayerMap[msg.channel().recipients?.first()]?.name ?: "Lol it's null",
                                    if (reactors.size == 2) "Approved" else "Rejected",
                                    inline = true)
                        }
                    }
                }
                continue@gameLoop
            } else {
                rejectedQuests = 0
            }

            channel.send {
                color = gold
                title = "The party has been accepted!"
                description = "Now ${party?.listGrammatically { it.name }} will either succeed or fail the quest"
                for (msg in reacts.keys) {
                    val reactors = msg.reactions(approveChar)
                    addField(userPlayerMap[msg.channel().recipients?.first()]?.name ?: "Lol it's null",
                            if (reactors.size == 2) "Approved" else "Rejected",
                            inline = true)
                }
            }
            reacts.clear()
            party!!.forEach { it.user.getDM().startTyping() }
            for (player in party!!) {
                val msg = player.user.sendDM("React ✔ to succeed the quest" + if (player.role?.loyalty == Evil) ", or ❌ to fail it" else "")
                reacts[msg] = 0
                Bot.launch {
                    msg.react(approveChar)
                    if (player.role?.loyalty == Evil)
                        msg.react(rejectChar)
                }
            }
            channel.states += Voting
            println("Everyone can now succeed/fail the quest")

            suspendUntil(25) {
                reacts.values.all { it.absoluteValue == 1 }
            }
            channel.states -= Voting
            val (successes, fails) = reacts.values.partition { it == 1 }.map { it.size }

            if (fails >= round.fails) {
                evilWins++
                channel.send {
                    color = Color.red
                    title = "There " + if (fails == 1) "was 1 fail" else "were $fails fails"
                    description = "Reminder: ${party?.listGrammatically { it.name }} were on this quest"
                }
            } else {
                goodWins++
                channel.send {
                    color = Color.blue
                    title = if (fails == 0) "All $successes were successes" else "There was $fails fail, but ${round.fails} are required this round"
                    description = "Reminder: ${party?.listGrammatically { it.name }} were on this quest"
                }
            }.apply {
                pin()
                this@Avalon.pinnedMessages += this
            }

            reacts.clear()

            when (3) {
                goodWins -> {
                    return@game if (Assassin in roles && Merlin in roles) {
                        channel.send {
                            title = "The good guys have succeeded three quests, but the Assassin can try to kill Merlin"
                            description = "Assassin, use ${"!assassinate <name>".inlineCode()} to assassinate who you think Merlin is"
                            players.filter { it.role?.loyalty == Evil }
                                    .forEach { addField(it.name.underline(), "${it.role?.name}", inline = true) }
                        }
                        var merlinGuess: Player? = null
                        val assassinateListener: suspend Message.() -> Unit = { // TODO make this a command
                            if (userPlayerMap[author]?.role == Assassin && content.startsWith("!ass") && mentions.size == 1) {
                                userPlayerMap[mentions.first()]?.let { target ->
                                    if (target.role?.loyalty == Good) {
                                        merlinGuess = target
                                    }
                                }
                            }
                        }
                        // todo make this a command
                        Bot.on(MessageCreate, MessageUpdate, λ = assassinateListener)
                        suspendUntil { merlinGuess != null }
                        Bot.off(MessageCreate, MessageUpdate, λ = assassinateListener)

                        GameFinish {
                            state.players.forEach { addField(it.name.underline(), "${it.role?.name}", inline = true) }
                            if ((merlinGuess!! as AvalonPlayer).role == Merlin) {
                                color = Color.red
                                title = "Correct! ${merlinGuess!!.name} was Merlin! The bad guys win!"
                            } else {
                                color = Color.blue
                                title = "Incorrect! The good guys win!"
                            }
                        }
                    } else {
                        GameFinish {
                            color = Color.blue
                            title = "The good guys win!"
                            players.forEach { addField(it.name.underline(), "${it.role?.name}", inline = true) }
                        }
                    }
                }
                evilWins -> {
                    return@game GameFinish {
                        color = Color.red
                        title = "The bad guys win!"
                        players.forEach { addField(it.name.underline(), "${it.role?.name}", inline = true) }
                    }
                }
            }

            if (ladyEnabled && roundNum in 2..4) {
                channel.states += State.Avalon.Lady
                channel.send {
                    title = "Now ${ladyOfTheLake!!.name} will use the Lady of the Lake on someone to find their alignment"
                    description = "use ${"!lotl".inlineCode()} and a player's name/username"
                }
                suspendUntil { ladyTarget != null }
                channel.states -= State.Avalon.Lady

                ladyOfTheLake!!.user.sendDM {
                    title = "${ladyTarget!!.name} is ${ladyTarget!!.role?.loyalty}"
                    image(ladyTarget!!.role!!.loyalty.image)
                }

                ladyOfTheLake = ladyTarget
                ladyTarget = null
            }

            roundNum++
            leaderNum++
        }

        GameFinish {
            title = "I don't think this should happen?"
            color = gold
        }
    }


    override fun toString(): String {
        return "Avalon(state=$state)"
    }
}
