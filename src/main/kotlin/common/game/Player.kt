package common.game

import lib.model.user.User

open class Player(val user: User)

val Player.name: String
    get() = user.username