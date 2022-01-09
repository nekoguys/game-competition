package ru.selemilka.game.rps.model

data class RpsRound(
    val id: Id,
    val answers: List<RpsRoundAnswer> = emptyList(),
    val winner: RpsPlayer.Human? = null,
) {
    data class Id(
        val sessionId: RpsSession.Id,
        val number: Long,
    )
}

data class RpsRoundAnswer(
    val player: RpsPlayer.Human,
    val bet: Turn,
)

enum class Turn {
    ROCK,
    PAPER,
    SCISSORS;

    infix fun beats(other: Turn): Boolean =
        when (this) {
            ROCK -> other == SCISSORS
            PAPER -> other == ROCK
            SCISSORS -> other == PAPER
        }
}

