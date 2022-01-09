package ru.selemilka.game.rps.model

sealed interface RpsPlayer {
    data class Human(
        val sessionId: RpsSession.Id,
        val name: String,
    ) : RpsPlayer

    data class Internal(
        val sessionId: RpsSession.Id,
    ) : RpsPlayer
}



