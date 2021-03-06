package ru.nekoguys.game.persistence.competition.model

data class DbCompetitionRoundAnswer(
    var sessionId: Long,
    var roundNumber: Int,
    var teamId: Long,
    var value: Int,
    var income: Double?,
)
