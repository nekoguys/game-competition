package ru.selemilka.game.process

import ru.selemilka.game.Game
import ru.selemilka.game.GameAction

abstract class GameProcess<G: Game> {
    abstract suspend fun process(gameAction: GameAction<G>)
}