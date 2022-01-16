package ru.selemilka.game.rps.rule

import kotlinx.serialization.Serializable
import org.springframework.stereotype.Component
import ru.selemilka.game.core.base.CloseGameSessionRequest
import ru.selemilka.game.core.base.ResourceLocks
import ru.selemilka.game.rps.RpsGameMessage
import ru.selemilka.game.rps.RpsGameRule
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsStage
import ru.selemilka.game.rps.storage.RpsPlayerStorage
import ru.selemilka.game.rps.storage.RpsSessionStorage
import ru.selemilka.game.rps.util.buildResponse
import ru.selemilka.game.rps.util.deferCommand

@Serializable
sealed class RpsChangeStageMessage : RpsMessage() {
    @Serializable
    object GameStarted : RpsChangeStageMessage()

    @Serializable
    object GameFinished : RpsChangeStageMessage()
}

@Component
class ChangeStageRule(
    private val sessionStorage: RpsSessionStorage,
    private val playerStorage: RpsPlayerStorage,
) : RpsGameRule<RpsPlayer.Internal, RpsCommand.ChangeStage, RpsChangeStageMessage> {

    private val resourceLocks: ResourceLocks =
        ResourceLocks(
            shared = sortedSetOf(RpsPlayerStorage),
            unique = sortedSetOf(RpsSessionStorage),
        )

    override suspend fun getLocksFor(command: RpsCommand.ChangeStage): ResourceLocks =
        resourceLocks

    override suspend fun process(
        player: RpsPlayer.Internal,
        command: RpsCommand.ChangeStage,
    ): List<RpsGameMessage<RpsChangeStageMessage>> {
        return changeStage(player, command)
    }

    private suspend fun changeStage(
        player: RpsPlayer.Internal,
        command: RpsCommand.ChangeStage,
    ): List<RpsGameMessage<RpsChangeStageMessage>> {
        val new = command.newStage
        val old = sessionStorage.loadStage(player.sessionId)
        val allPlayers = playerStorage.loadPlayers(player.sessionId)

        return when {
            old == RpsStage.PLAYERS_JOINING && new == RpsStage.GAME_STARTED -> {
                sessionStorage.saveStage(player.sessionId, new)
                buildResponse(allPlayers) {
                    +RpsChangeStageMessage.GameStarted
                }
            }
            old == RpsStage.GAME_STARTED && new == RpsStage.GAME_FINISHED -> {
                sessionStorage.saveStage(player.sessionId, new)
                buildResponse {
                    allPlayers { +RpsChangeStageMessage.GameFinished }
                    deferCommand(CloseGameSessionRequest)
                }
            }
            else -> error("Stage change from $old to $new is impossible")
        }
    }
}
