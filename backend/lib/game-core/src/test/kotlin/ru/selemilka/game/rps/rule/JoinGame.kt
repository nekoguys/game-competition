package ru.selemilka.game.rps.rule

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

sealed interface JoinGameMessage {
    object YouJoinedGame : JoinGameMessage

    data class SomebodyJoinedGame(val name: String) : JoinGameMessage

    sealed interface Error : JoinGameMessage

    object YouAlreadyJoined : Error

    object SessionIsFull : Error
}

fun JoinGameMessage.toRoot(): RpsMessage.JoinGame =
    RpsMessage.JoinGame(this)

@Service
class RpsJoinGameRule(
    private val sessionStorage: RpsSessionStorage,
    private val playerStorage: RpsPlayerStorage,
) : RpsGameRule<RpsPlayer.Human, RpsCommand.JoinGame, RpsMessage.JoinGame> {

    private val resourceLocks = ResourceLocks(
        shared = sortedSetOf(RpsSessionStorage),
        unique = sortedSetOf(RpsPlayerStorage),
    )

    override suspend fun getLocksFor(command: RpsCommand.JoinGame): ResourceLocks =
        resourceLocks

    override suspend fun process(
        player: RpsPlayer.Human,
        command: RpsCommand.JoinGame,
    ): List<RpsGameMessage<RpsMessage.JoinGame>> {
        val settings = sessionStorage.loadSettings(player.sessionId)
        checkNotNull(settings) { "Session must be created at this point" }

        return joinGame(player, maxPlayers = settings.maxPlayers)
    }

    private suspend fun joinGame(
        newPlayer: RpsPlayer.Human,
        maxPlayers: Int,
    ): List<RpsGameMessage<RpsMessage.JoinGame>> {
        val playersAlreadyInSession = playerStorage.loadPlayers(newPlayer.sessionId)
        return when {
            newPlayer in playersAlreadyInSession -> buildResponse(newPlayer) {
                +JoinGameMessage.YouAlreadyJoined.toRoot()
            }

            playersAlreadyInSession.size == maxPlayers -> buildResponse(newPlayer) {
                +JoinGameMessage.SessionIsFull.toRoot()
            }

            else -> addPlayerToSession(newPlayer, playersAlreadyInSession, maxPlayers)
        }
    }

    private suspend fun addPlayerToSession(
        player: RpsPlayer.Human,
        playersAlreadyInSession: List<RpsPlayer.Human>,
        maxPlayers: Int,
    ): List<RpsGameMessage<RpsMessage.JoinGame>> {
        playerStorage.savePlayer(player)

        return buildResponse {
            player { +JoinGameMessage.YouJoinedGame.toRoot() }

            (playersAlreadyInSession - player) {
                +JoinGameMessage.SomebodyJoinedGame(player.name).toRoot()
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
