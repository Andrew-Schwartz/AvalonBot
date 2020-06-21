package kittens.game

import common.game.GameConfig

class KittensConfig : GameConfig {
    var implodingKittens = false

    override fun reset() {}

    override fun toString(): String {
        return "KittensConfig(implodingKittens=$implodingKittens)"
    }
}