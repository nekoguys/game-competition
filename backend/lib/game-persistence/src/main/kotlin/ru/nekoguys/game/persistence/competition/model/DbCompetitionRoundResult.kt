package ru.nekoguys.game.persistence.competition.model

data class DbCompetitionRoundResult(
    var sessionId: Long,
    var roundNumber: Int,
    var teamId: Long,
    var income: Double,
)
