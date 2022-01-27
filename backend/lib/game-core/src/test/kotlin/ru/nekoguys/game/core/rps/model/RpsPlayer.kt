package ru.nekoguys.game.core.rps.model

import kotlinx.serialization.Serializable

@Serializable
sealed class RpsPlayer {
    @Serializable
    data class Human(
        val sessionId: RpsSession.Id,
        val name: String,
    ) : RpsPlayer()

    @Serializable
    data class Internal(
        val sessionId: RpsSession.Id,
    ) : RpsPlayer()
}



