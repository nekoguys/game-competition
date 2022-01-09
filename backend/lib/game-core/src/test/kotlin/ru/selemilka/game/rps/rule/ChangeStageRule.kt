package ru.selemilka.game.rps.rule

import org.springframework.stereotype.Component
import ru.selemilka.game.core.base.CloseGameSessionRequest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.base.GameRule
import ru.selemilka.game.core.base.LockedResources
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsStage
import ru.selemilka.game.rps.storage.RpsPlayerStorage
import ru.selemilka.game.rps.storage.RpsSessionStorage
import ru.selemilka.game.rps.util.buildResponse

sealed interface ChangeStageMessage {
    object GameStarted : ChangeStageMessage

    object GameFinished : ChangeStageMessage

    data class StageChangeIsImpossible(
        val previous: RpsStage?,
        val requested: RpsStage,
    ) : ChangeStageMessage
}

fun ChangeStageMessage.toRoot(): RpsMessage.StageChanged =
    RpsMessage.StageChanged(this)

@Component
class ChangeStageRule(
    private val sessionStorage: RpsSessionStorage,
    private val playerStorage: RpsPlayerStorage,
) : GameRule<RpsPlayer, RpsCommand.ChangeStage, RpsMessage> {

    override suspend fun getLocksFor(command: RpsCommand.ChangeStage): LockedResources =
        LockedResources(
            shared = setOf(RpsPlayerStorage),
            unique = setOf(RpsSessionStorage),
        )

    override suspend fun process(
        player: RpsPlayer,
        command: RpsCommand.ChangeStage,
    ): List<GameMessage<RpsPlayer, RpsMessage>> {
        if (player !is RpsPlayer.Internal) {
            return buildResponse(player) {
                +RpsMessage.UnableRequestCommand(player, expectedClass = RpsPlayer.Internal::class)
            }
        }

        return changeStage(player, command)
    }

    private suspend fun changeStage(
        player: RpsPlayer.Internal,
        command: RpsCommand.ChangeStage,
    ): List<GameMessage<RpsPlayer, RpsMessage.StageChanged>> {
        val new = command.newStage
        val old = sessionStorage.loadStage(player.sessionId)
        val allPlayers = playerStorage.loadPlayers(player.sessionId)

        return when {
            old == RpsStage.PLAYERS_JOINING && new == RpsStage.GAME_STARTED -> {
                sessionStorage.saveStage(player.sessionId, new)
                buildResponse(allPlayers) {
                    +ChangeStageMessage.GameStarted.toRoot()
                }
            }
            old == RpsStage.GAME_STARTED && new == RpsStage.GAME_FINISHED -> {
                sessionStorage.saveStage(player.sessionId, new)
                buildResponse {
                    allPlayers { +ChangeStageMessage.GameFinished.toRoot() }
                    deferCommand(CloseGameSessionRequest)
                }
            }
            else -> buildResponse(player) {
                +ChangeStageMessage.StageChangeIsImpossible(old, new).toRoot()
            }
        }
    }
}
