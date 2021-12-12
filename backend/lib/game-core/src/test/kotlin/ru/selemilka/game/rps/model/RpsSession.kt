package ru.selemilka.game.rps.model

data class RpsSession(
    val id: Id,
    val stage: RpsStage,
    val settings: RpsSessionSettings = RpsSessionSettings(),
) {
    @JvmInline
    value class Id(val number: Long)
}

enum class RpsStage {
    PLAYERS_JOINING,
    GAME_STARTED,
    GAME_FINISHED,
}

data class RpsSessionSettings(
    val maxPlayers: Int = 2,
)
