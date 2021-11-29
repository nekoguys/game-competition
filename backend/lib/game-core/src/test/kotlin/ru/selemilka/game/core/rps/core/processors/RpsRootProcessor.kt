package ru.selemilka.game.core.rps.core.processors

import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.base.TypedProcessor
import ru.selemilka.game.core.rps.RpsPlayer
import ru.selemilka.game.core.rps.RpsPlayerAction
import ru.selemilka.game.core.rps.RpsPlayerReaction
import ru.selemilka.game.core.rps.RpsPlayerScope
import ru.selemilka.game.core.rps.core.RpsGameStateStorage
import ru.selemilka.game.core.rps.core.RpsPlayerStorage
import ru.selemilka.game.core.rps.core.RpsRootStageStorage

class RpsRootProcessor(
    private val gameStateStorage: RpsGameStateStorage,
    private val playerStorage: RpsPlayerStorage,
    private val rootStageStorage: RpsRootStageStorage
) : TypedProcessor<RpsPlayerAction, RpsPlayerReaction> {
    override val actionClass = RpsPlayerAction::class
    private val joiningProcessor = RpsPlayerJoiningProcessor(playerStorage)
    private val gameProcessor = RpsGameProcessor(gameStateStorage)

    override suspend fun process(id: SessionId, action: RpsPlayerAction): List<RpsPlayerReaction> {
        when (action) {
            is RpsPlayerAction.CreateGame -> {
                if (rootStageStorage.stageFor(id) == null) {
                    rootStageStorage.initGame(id)
                    return emptyList()
                }
                return listOf(RpsPlayerReaction.BadGameStage(RpsPlayerScope(action.initiator)))
            }
            is RpsPlayerAction.JoinGame -> {
                if (rootStageStorage.stageFor(id) == RpsRootStageStorage.JoiningStage) {
                    val result = joiningProcessor.process(id, action)
                    if (playerStorage.getPlayers(id).size == 2) {
                        rootStageStorage.joiningEnded(id)
                    }
                    return result
                }
                return listOf(RpsPlayerReaction.BadGameStage(RpsPlayerScope(action.initiator)))
            }
            is RpsPlayerAction.Turn -> {
                if (rootStageStorage.stageFor(id) == RpsRootStageStorage.GameStage) {
                    val result = gameProcessor.process(id, action)
                    if (gameStateStorage.isGameEnded(id)) {
                        rootStageStorage.gameEnded(id)
                    }
                    return result
                }
                return listOf(RpsPlayerReaction.BadGameStage(RpsPlayerScope(action.initiator)))
            }
        }
    }
}