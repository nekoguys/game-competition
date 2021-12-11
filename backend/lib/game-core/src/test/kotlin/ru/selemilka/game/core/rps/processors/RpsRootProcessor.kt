package ru.selemilka.game.core.rps.processors

import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.base.TypedProcessor
import rps.RpsPlayerCommand
import rps.RpsPlayerMessage
import ru.selemilka.game.core.rps.RpsPlayerScope
import ru.selemilka.game.core.rps.storage.RpsGameStateStorage
import ru.selemilka.game.core.rps.storage.RpsPlayerStorage
import ru.selemilka.game.core.rps.storage.RpsRootStageStorage

class RpsRootProcessor(
    private val gameStateStorage: RpsGameStateStorage,
    private val playerStorage: RpsPlayerStorage,
    private val rootStageStorage: RpsRootStageStorage
) : TypedProcessor<RpsPlayerCommand, RpsPlayerMessage> {
    override val actionClass = RpsPlayerCommand::class
    private val joiningProcessor = RpsPlayerJoiningProcessor(playerStorage)
    private val gameProcessor = RpsGameProcessor(gameStateStorage)

    override suspend fun process(id: SessionId, action: RpsPlayerCommand): List<RpsPlayerMessage> {
        when (action) {
            is RpsPlayerCommand.CreateGame -> {
                if (rootStageStorage.stageFor(id) == null) {
                    rootStageStorage.initGame(id)
                    return emptyList()
                }
                return listOf(RpsPlayerMessage.BadGameStage(RpsPlayerScope(action.initiator)))
            }
            is RpsPlayerCommand.JoinGame -> {
                if (rootStageStorage.stageFor(id) == RpsRootStageStorage.JoiningStage) {
                    val result = joiningProcessor.process(id, action)
                    if (playerStorage.getPlayers(id).size == 2) {
                        rootStageStorage.joiningEnded(id)
                    }
                    return result
                }
                return listOf(RpsPlayerMessage.BadGameStage(RpsPlayerScope(action.initiator)))
            }
            is RpsPlayerCommand.Turn -> {
                if (rootStageStorage.stageFor(id) == RpsRootStageStorage.GameStage) {
                    val result = gameProcessor.process(id, action)
                    if (gameStateStorage.isGameEnded(id)) {
                        rootStageStorage.gameEnded(id)
                    }
                    return result
                }
                return listOf(RpsPlayerMessage.BadGameStage(RpsPlayerScope(action.initiator)))
            }
        }
    }
}
