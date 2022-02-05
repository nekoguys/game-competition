package ru.nekoguys.game.entity.competition.model

import ru.nekoguys.game.entity.commongame.model.CommonSession

data class CompetitionTeam(
    val id: Id,
    val sessionId: CommonSession.Id,
    val name: String,
    val captain: CompetitionPlayer.TeamCaptain,
    val teamMates: List<CompetitionPlayer.TeamMate>,
    val isBanned: Boolean,
) {
    data class Id(val number: Long)
}
