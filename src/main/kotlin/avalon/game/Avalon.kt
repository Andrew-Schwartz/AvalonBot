package avalon.game

import avalon.characters.Assassin
import avalon.characters.Character.Loyalty.Evil
import avalon.characters.Character.Loyalty.Good
import avalon.characters.LoyalServant
import avalon.characters.Merlin
import avalon.characters.MinionOfMordred
import common.bot
import common.commands.State
import common.commands.debug
import common.commands.states
import common.commands.subStates
import common.game.*
import common.util.A
import common.util.listGrammatically
import common.util.map
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.dsl.RichEmbed
import lib.dsl.off
import lib.dsl.on
import lib.dsl.suspendUntil
import lib.model.Color
import lib.model.Color.Companion.gold
import lib.model.channel.Message
import lib.rest.http.httpRequests.deletePin
import lib.rest.http.httpRequests.getMessage
import lib.rest.model.events.receiveEvents.MessageCreate
import lib.rest.model.events.receiveEvents.MessageReactionUpdate
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Add
import lib.rest.model.events.receiveEvents.MessageReactionUpdatePayload.Type.Remove
import lib.rest.model.events.receiveEvents.MessageUpdate
import lib.util.inlineCode
import lib.util.ping
import lib.util.underline
import kotlin.math.absoluteValue

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class Avalon(setup: Setup) : Game(GameType.Avalon, setup) {
    internal val state = AvalonState(this, setup)

    override suspend fun startGame(): Unit = bot.run {
        with(state) {
            rounds = Rounds(players.size)
            val numGood = players.size - state.numEvil
            val (good, evil) = roles.partition { it.loyalty == Good }.run { first.size to second.size }

            for (i in good until numGood) roles.add(LoyalServant)
            for (i in evil until numEvil) roles.add(MinionOfMordred)

            for (player in players) {
                roles.random().let {
                    player.role = it
                    roles -= it
                }
            }
            players.shuffle()
            if (ladyEnabled) {
                ladyOfTheLake = players.last()
            }

            if (roles.isNotEmpty()) throw RuntimeException("ROLES WERE NOT EMPTY")
            roles += players.map { it.role!! }

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
                                            .filter { debug || it.name != player.name }
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

            GlobalScope.launch {
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
                    val approveChar = '✔'
                    val rejectChar = '❌'
//                    val messages = arrayListOf<Message>()
                    val reacts = mutableMapOf<Message, Int>() // -1 = reject, +1 = approve
                    for (player in players) {
                        val msg = player.user.sendDM("React ✔ to vote to approve the quest, or ❌ to reject it\n" +
                                "The proposed party is ${party?.listGrammatically { it.name }}")
//                        messages += msg
                        reacts[msg] = 0
                        launch {
                            msg.react(approveChar)
                            msg.react(rejectChar)
                        }
                    }
                    val reactListener: suspend MessageReactionUpdatePayload.() -> Unit = {
                        val msg = getMessage(channelId, messageId)
                        if (user.isBot == false && msg in reacts.keys) {
                            val delta = when (emoji.name[0]) {
                                approveChar -> 1
                                rejectChar -> -1
                                else -> 0
                            } * when (type) {
                                Add -> 1
                                Remove -> -1
                            }
                            reacts[msg] = reacts[msg]!! + delta
                        }
                    }
                    on(MessageReactionUpdate, λ = reactListener)
                    println("All players can now vote on the party")

                    suspendUntil(50) {
//                        messages.all { (it.reactions(approveChar).size == 2) xor (it.reactions(rejectChar).size == 2) }
                        reacts.values.all { it.absoluteValue == 1 }
                    }
                    off(MessageReactionUpdate, λ = reactListener)
//                    val (approve, reject) = messages.partition { it.reactions(approveChar).size == 2 }.map { it.size }
                    val (approve, reject) = reacts.values.partition { it == 1 }.map { it.size }
                    if (reject >= approve) {
                        leaderNum++
                        rejectedQuests++
                        if (rejectedQuests == 5) {
                            channel.send {
                                color = Color.red
                                title = "There have been 5 rejected parties in a row so the bad guys win"
                            }
                            break@gameLoop
                        } else {
                            channel.send {
                                title = when (rejectedQuests) {
                                    1 -> "There is now 1 reject"
                                    else -> "There are now $rejectedQuests rejects in a row"
                                }
                                for (msg in reacts.keys) {
                                    val reactors = msg.reactions(approveChar)
                                    addField(userPlayerMap[msg.channel.recipients?.first()]?.name ?: "Lol it's null",
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
                            addField(userPlayerMap[msg.channel.recipients?.first()]?.name ?: "Lol it's null",
                                    if (reactors.size == 2) "Approved" else "Rejected",
                                    inline = true)
                        }
                    }
                    reacts.clear()
                    party!!.forEach { it.user.getDM().startTyping() }
                    for (player in party!!) {
                        val msg = player.user.sendDM("React ✔ to succeed the quest" + if (player.role?.loyalty == Evil) ", or ❌ to fail it" else "")
//                        messages += msg
                        reacts[msg] = 0
                        launch {
                            msg.react(approveChar)
                            if (player.role?.loyalty == Evil)
                                msg.react(rejectChar)
                        }
                    }
                    on(MessageReactionUpdate, λ = reactListener)
                    println("Everyone can now succeed/fail the quest")


                    suspendUntil(50) {
//                        messages.all { msg ->
//                            msg.reactions(approveChar).size == 2 || msg.reactions(rejectChar).size == 2
//                        }
                        reacts.values.all { it.absoluteValue == 1 }
                    }
                    off(MessageReactionUpdate, λ = reactListener)
                    val (successes, fails) = reacts.values.partition { it == 1 }.map { it.size }

                    if (fails >= round.fails) {
                        evilWins++
                        channel.send {
                            color = Color.red
                            title = "There " + if (fails == 1) "was 1 fail" else "were $fails fails"
                            description = "Reminder: ${party?.listGrammatically { it.name }} were on this quest"
                        }.apply {
                            pin()
                            this@Avalon.pinnedMessages += this
                        }
                    } else {
                        goodWins++
                        channel.send {
                            color = Color.blue
                            title = if (fails == 0) "All $successes were successes" else "There was $fails fail, but ${round.fails} are required this round"
                            description = "Reminder: ${party?.listGrammatically { it.name }} were on this quest"
                        }.apply {
                            pin()
                            this@Avalon.pinnedMessages += this
                        }
                    }

                    when (3) {
                        goodWins -> {
                            if (Assassin in roles && Merlin in roles) {
                                channel.send {
                                    title = "The good guys have succeeded three quests, but the Assassin can try to kill Merlin"
                                    description = "Assassin, use ${"!assassinate <name>".inlineCode()} to assassinate who you think Merlin is"
                                    players.filter { it.role?.loyalty == Evil }
                                            .forEach { addField(it.name.underline(), "${it.role?.name}", inline = true) }
                                }
                                var merlinGuess: Player? = null
                                val assassinateListener: suspend Message.() -> Unit = {
                                    if (userPlayerMap[author]?.role == Assassin && content.startsWith("!ass")) {
                                        val name = args[0]
                                        merlinGuess = playerByName(name)
                                    }
                                }
                                on(MessageCreate, MessageUpdate, λ = assassinateListener)

//                                launch {
                                suspendUntil { merlinGuess != null }
                                off(MessageCreate, MessageUpdate, λ = assassinateListener)

                                if ((merlinGuess!! as AvalonPlayer).role == Merlin) {
                                    channel.send {
                                        color = Color.red
                                        title = "Correct! ${merlinGuess!!.name} was Merlin! The bad guys win!"
                                        revealAllRoles()
                                    }
                                } else {
                                    channel.send {
                                        color = Color.blue
                                        title = "Incorrect! The good guys win!"
                                        revealAllRoles()
                                    }
                                }
//                                }
                            } else {
                                channel.send {
                                    color = Color.blue
                                    title = "The good guys win!"
                                    players.forEach { addField(it.name.underline(), "${it.role?.name}", inline = true) }
                                }
                            }
                            break@gameLoop
                        }
                        evilWins -> {
                            channel.send {
                                color = Color.red
                                title = "The bad guys win!"
                                players.forEach { addField(it.name.underline(), "${it.role?.name}", inline = true) }
                            }
                            break@gameLoop
                        }
                    }

                    if (ladyEnabled && roundNum in 2..4) {
                        channel.states += State.Avalon.Lady
                        channel.send {
                            title = "Now ${ladyOfTheLake!!.name} will use the Lady of the Lake on someone to find their alignment"
                            description = "use ${"!lady".inlineCode()} and a player's name/username"
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

                stopGame("Game Completed!")
            }
        }
    }

    override suspend fun stopGame(message: String) {
        bot.run {
            channel.send {
                title = "Ending game"
                description = message
                color = Color.red
            }
        }
        cleanup()
    }

    internal fun playerByName(name: String?): AvalonPlayer? {
        name ?: return null
        return state.players.firstOrNull {
            it.name.equals(name, ignoreCase = true) || it.name.equals(name, ignoreCase = true)
        }
    }

    private fun RichEmbed.revealAllRoles() {
        state.players.forEach { addField(it.name.underline(), "${it.role?.name}", inline = true) }
    }

    private suspend fun cleanup() = bot.run {
        channel.states.removeAll(subStates<State.Avalon>())
        this@Avalon.pinnedMessages.forEach { pin ->
            this@run.pinnedMessages -= pin
            runCatching { deletePin(pin.channelId, pin.id) }
                    .onFailure { println(it.message) }
        }
    }
}
