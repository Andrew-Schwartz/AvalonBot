package hangman.game

import common.game.GameConfig
import hangman.RandomWord

class HangmanConfig : GameConfig {
    var randomWord: RandomWord? = null

    override fun reset() {}
}