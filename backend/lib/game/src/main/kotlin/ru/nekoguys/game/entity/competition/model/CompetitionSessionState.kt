package ru.nekoguys.game.entity.competition.model

data class CompetitionSessionState(
    val stage: CompetitionStage,
    val team: List<CompetitionTeam>,
)
