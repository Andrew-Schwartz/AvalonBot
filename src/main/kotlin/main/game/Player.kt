package main.game

import lib.model.user.User

open class Player(val name: String, val user: User) {
    val username = user.username

    val nameAndUser = "$name ($username)"
}