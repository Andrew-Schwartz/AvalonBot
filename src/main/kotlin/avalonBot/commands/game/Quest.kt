//package avalonBot.commands.game
//
//import avalonBot.commands.Command
//import avalonBot.game.Avalon
//import avalonBot.game.Rounds
//import avalonBot.players
//import io.ktor.util.KtorExperimentalAPI
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import lib.dsl.Bot
//import lib.model.Message
//
//class Quest : Command(1) {
//    override val name: String = "quest"
//
//    override val description: String = "used by the leader to send people on a quest"
//
//    override val usage: String = "!quest <person1> <person2>..."
//
//    @KtorExperimentalAPI
//    @ExperimentalCoroutinesApi
//    override val execute: suspend Bot.(Message, args: List<String>) -> Unit = { message, args ->
//        message.reply("leaderChooseListener")
//        println("leaderChooseListener")
//        if (message.author == Avalon.gamePlayers[roundNum].user && content.startsWith("!quest")) {
//            val questers = args.mapNotNull { arg ->
//                try {
//                    gamePlayers.first { it.name == arg }
//                } catch (e: NoSuchElementException) {
//                    bot.run { reply("No one by the (nick)name $arg") }
//                    null
//                }
//            }.toSet()
//
//            val targetNum = Rounds[players.size, roundNum].players
//            bot.run {
//                when (questers.size) {
//                    targetNum -> questPeople = questers
//                    else -> reply("You need to send $targetNum people!", ping = true)
//                }
//            }
//        } else {
//            println("$author != ${gamePlayers[roundNum].user}")
//        }
//    }
//}