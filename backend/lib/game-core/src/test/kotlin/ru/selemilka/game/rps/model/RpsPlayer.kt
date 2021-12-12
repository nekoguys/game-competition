package ru.selemilka.game.rps.model

/**
 * У игрока нет своего ID, так как у него все колонки - ключевые
 */
data class RpsPlayer(
    val sessionId: RpsSession.Id,
    val name: String,
    val synthetic: Boolean = false,
)

fun getSyntheticPlayer(sessionId: RpsSession.Id) =
    RpsPlayer(
        sessionId = sessionId,
        name = "robot",
        synthetic = true,
    )
