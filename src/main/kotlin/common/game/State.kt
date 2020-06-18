package common.game

abstract class State<P : Player> {
    abstract val players: ArrayList<P>
}