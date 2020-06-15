package kittens.game

import common.game.GameConfig

class KittensConfig : GameConfig {
    var implodingKittens = false

    override fun toString(): String {
        return "KittensConfig(implodingKittens=$implodingKittens)"
    }
}