package ru.selemilka.game.core.process

import ru.selemilka.game.core.Game
import ru.selemilka.game.core.model.GameAction

abstract class GameProcess<G : Game> {
    abstract suspend fun process(gameAction: GameAction<G>)
}
