package kittens.game

import common.game.GameConfig

class KittenConfig : GameConfig {
    var implodingKittens = false

    override fun reset() {}

    override fun toString(): String {
        return "KittensConfig(implodingKittens=$implodingKittens)"
    }
}