package ru.selemilka.game.rps.model

import kotlinx.serialization.Serializable

data class RpsSession(
    val id: Id,
    val stage: RpsStage,
    val settings: RpsSessionSettings = RpsSessionSettings(),
) {
    @Serializable
    @JvmInline
    value class Id(val value: Long)
}

enum class RpsStage {
    PLAYERS_JOINING,
    GAME_STARTED,
    GAME_FINISHED,
}

data class RpsSessionSettings(
    val maxPlayers: Int = 2,
    val rounds: Int = 3,
)
