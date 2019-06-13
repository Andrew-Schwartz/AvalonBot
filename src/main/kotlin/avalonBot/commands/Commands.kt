package avalonBot.commands

import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.util.EnhancedEventListener

val commands: ArrayList<Command> = ArrayList()

interface Command {
    val name: String

    val description: String

    val usage: String

    val execute: EnhancedEventListener.(Message) -> Unit
}