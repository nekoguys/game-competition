package ru.selemilka.game.rps.rule

import kotlinx.serialization.Serializable
import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.ResourceLocks
import ru.selemilka.game.rps.RpsGameMessage
import ru.selemilka.game.rps.RpsGameRule
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsStage
import ru.selemilka.game.rps.storage.RpsPlayerStorage
import ru.selemilka.game.rps.storage.RpsSessionStorage
import ru.selemilka.game.rps.util.buildResponse
import ru.selemilka.game.rps.util.deferCommand

sealed class RpsJoinGameMessage : RpsMessage() {
    @Serializable
    object YouJoinedGame : RpsJoinGameMessage()

    @Serializable
    data class SomebodyJoinedGame(val name: String) : RpsJoinGameMessage()
}

sealed class JoinGameMessageError : RpsJoinGameMessage() {
    @Serializable
    object YouAlreadyJoined : JoinGameMessageError()

    @Serializable
    object SessionIsFull : JoinGameMessageError()
}

@Service
class RpsJoinGameRule(
    private val sessionStorage: RpsSessionStorage,
    private val playerStorage: RpsPlayerStorage,
) : RpsGameRule<RpsPlayer.Human, RpsCommand.JoinGame, RpsJoinGameMessage> {

    private val resourceLocks = ResourceLocks(
        shared = sortedSetOf(RpsSessionStorage),
        unique = sortedSetOf(RpsPlayerStorage),
    )

    override suspend fun getLocksFor(command: RpsCommand.JoinGame): ResourceLocks =
        resourceLocks

    override suspend fun process(
        player: RpsPlayer.Human,
        command: RpsCommand.JoinGame,
    ): List<RpsGameMessage<RpsJoinGameMessage>> {
        val settings = sessionStorage.loadSettings(player.sessionId)
        checkNotNull(settings) { "Session must be created at this point" }

        return joinGame(player, maxPlayers = settings.maxPlayers)
    }

    private suspend fun joinGame(
        newPlayer: RpsPlayer.Human,
        maxPlayers: Int,
    ): List<RpsGameMessage<RpsJoinGameMessage>> {
        val playersAlreadyInSession = playerStorage.loadPlayers(newPlayer.sessionId)
        return when {
            newPlayer in playersAlreadyInSession -> buildResponse(newPlayer) {
                +JoinGameMessageError.YouAlreadyJoined
            }

            playersAlreadyInSession.size == maxPlayers -> buildResponse(newPlayer) {
                +JoinGameMessageError.SessionIsFull
            }

            else -> addPlayerToSession(newPlayer, playersAlreadyInSession, maxPlayers)
        }
    }

    private suspend fun addPlayerToSession(
        player: RpsPlayer.Human,
        playersAlreadyInSession: List<RpsPlayer.Human>,
        maxPlayers: Int,
    ): List<RpsGameMessage<RpsJoinGameMessage>> {
        playerStorage.savePlayer(player)

        return buildResponse {
            player { +RpsJoinGameMessage.YouJoinedGame }

            (playersAlreadyInSession - player) {
                +RpsJoinGameMessage.SomebodyJoinedGame(player.name)
            }

            // Последний игрок подключился к игре
            if (playersAlreadyInSession.size == maxPlayers - 1) {
                deferCommand(
                    fromPlayer = RpsPlayer.Internal(player.sessionId),
                    command = RpsCommand.ChangeStage(RpsStage.GAME_STARTED)
                )
            }
        }
    }
}
