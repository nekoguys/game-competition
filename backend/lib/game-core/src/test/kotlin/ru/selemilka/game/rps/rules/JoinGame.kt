package ru.selemilka.game.rps.rules

import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.TargetedMessage
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.model.RpsStage
import ru.selemilka.game.rps.storage.RpsPlayerStorage
import ru.selemilka.game.rps.storage.RpsSessionStorage

sealed interface JoinGameMessage {
    object YouJoinedGame : JoinGameMessage
    data class SomebodyJoinedGame(val name: String) : JoinGameMessage
    object GameStarted : JoinGameMessage

    sealed interface Error : JoinGameMessage
    object YouAlreadyJoined : Error
    object SessionIsFull : Error
}

fun JoinGameMessage.intoRoot(): RpsRootMessage.JoinGame =
    RpsRootMessage.JoinGame(this)

@Service
class RpsJoinGameRule(
    sessionStorage: RpsSessionStorage,
    private val playerStorage: RpsPlayerStorage,
) : RpsRootSubProcessor<RpsRootCommand.JoinGame, RpsRootMessage.JoinGame>(sessionStorage) {

    override val expectedStages = setOf(RpsStage.PLAYERS_JOINING)

    override suspend fun process(
        player: RpsPlayer,
        command: RpsRootCommand.JoinGame,
        settings: RpsSessionSettings,
    ): List<RpsResponse<RpsRootMessage>> =
        joinGame(player, maxPlayers = settings.maxPlayers)
            .map { TargetedMessage(it.player, it.message.intoRoot()) }

    private suspend fun joinGame(
        newPlayer: RpsPlayer,
        maxPlayers: Int,
    ): List<RpsResponse<JoinGameMessage>> {
        val playersAlreadyInSession = playerStorage.loadPlayers(newPlayer.sessionId)
        return when {
            newPlayer in playersAlreadyInSession -> respond(newPlayer) {
                +JoinGameMessage.YouAlreadyJoined
            }

            playersAlreadyInSession.size == maxPlayers -> respond(newPlayer) {
                +JoinGameMessage.SessionIsFull
            }

            else -> addPlayerToSession(newPlayer, playersAlreadyInSession)
        }
    }

    private suspend fun addPlayerToSession(
        player: RpsPlayer,
        playersAlreadyInSession: List<RpsPlayer>,
    ): List<RpsResponse<JoinGameMessage>> {
        playerStorage.savePlayer(player)
        return respond {
            player { +JoinGameMessage.YouJoinedGame }

            (playersAlreadyInSession - player) {
                +JoinGameMessage.SomebodyJoinedGame(player.name)
            }

            // Последний игрок подключился к игре
            if (playersAlreadyInSession.size == 1) {
//                startFirstRound(player.sessionId)
                playersAlreadyInSession { +JoinGameMessage.GameStarted }
            }
        }
    }
}
