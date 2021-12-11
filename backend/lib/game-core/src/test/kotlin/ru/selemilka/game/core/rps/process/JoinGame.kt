package ru.selemilka.game.core.rps.process

import ru.selemilka.game.core.rps.storage.RpsPlayerStorage

sealed interface JoinGameMessage : RpsMessage {
    object YouJoinedGame : JoinGameMessage
    data class SomebodyJoinedGame(val name: String) : JoinGameMessage

    object YouAlreadyJoined : JoinGameMessage
    object ThereAreTwoPlayersInSession : JoinGameMessage
}

class RpsJoinGameProcessor(
    private val playerStorage: RpsPlayerStorage,
) {
    suspend fun joinGame(
        player: RpsPlayer,
    ): List<RpsAnnouncement<JoinGameMessage>> {
        val playerNames = playerStorage.getPlayers(player.session)

        return when {
            player.name in playerNames -> announces {
                JoinGameMessage.YouAlreadyJoined to player
            }

            playerNames.size == 2 -> announces {
                JoinGameMessage.ThereAreTwoPlayersInSession to player
            }

            else -> {
                playerStorage.addPlayer(player.session, player.name)
                announces {
                    JoinGameMessage.YouJoinedGame to player
                    JoinGameMessage.SomebodyJoinedGame(player.name) to player.session
                }
            }
        }
    }
}

