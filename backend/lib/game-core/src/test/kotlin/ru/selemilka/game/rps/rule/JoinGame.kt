package ru.selemilka.game.rps.rule

import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.base.GameRule
import ru.selemilka.game.core.base.LockedResources
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsStage
import ru.selemilka.game.rps.storage.RpsPlayerStorage
import ru.selemilka.game.rps.storage.RpsSessionStorage
import ru.selemilka.game.rps.util.buildResponse

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
) : GameRule<RpsPlayer, RpsCommand.JoinGame, RpsMessage> {

    override suspend fun getLocksFor(command: RpsCommand.JoinGame): LockedResources =
        LockedResources(
            shared = setOf(RpsSessionStorage),
            unique = setOf(RpsPlayerStorage),
        )

    override suspend fun process(
        player: RpsPlayer,
        command: RpsCommand.JoinGame,
    ): List<GameMessage<RpsPlayer, RpsMessage>> {
        if (player !is RpsPlayer.Human) {
            return buildResponse(player) {
                +RpsMessage.UnableRequestCommand(player, expectedClass = RpsPlayer.Human::class)
            }
        }

        val settings = sessionStorage.loadSettings(player.sessionId)
        checkNotNull(settings) { "Session must be created at this point" }

        return joinGame(player, maxPlayers = settings.maxPlayers)
    }

    private suspend fun joinGame(
        newPlayer: RpsPlayer.Human,
        maxPlayers: Int,
    ): List<GameMessage<RpsPlayer, RpsMessage>> {
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
    ): List<GameMessage<RpsPlayer, RpsMessage>> {
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
