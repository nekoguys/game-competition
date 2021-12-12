package ru.selemilka.game.rps.model

data class RpsRound(
    val id: Id,
    val answers: List<RpsRoundAnswer> = emptyList(),
    val winner: RpsPlayer? = null,
) {
    data class Id(
        val sessionId: RpsSession.Id,
        val number: Long,
    )
}

data class RpsRoundAnswer(
    val player: RpsPlayer,
    val bet: Turn,
)

enum class Turn {
    ROCK,
    PAPER,
    SCISSORS,
}

infix fun Turn.beats(other: Turn): Boolean =
    when (this) {
        Turn.ROCK -> other == Turn.SCISSORS
        Turn.PAPER -> other == Turn.ROCK
        Turn.SCISSORS -> other == Turn.SCISSORS
    }
