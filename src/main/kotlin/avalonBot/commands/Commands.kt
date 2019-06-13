package avalonBot.commands

import lib.model.Message

val commands: ArrayList<Command> = ArrayList()

interface Command {
    val name: String

    val description: String

    val usage: String

    val execute: (Message) -> Unit
}