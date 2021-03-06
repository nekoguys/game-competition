package ru.nekoguys.game.entity.competition.model

import ru.nekoguys.game.entity.commongame.model.CommonSession

data class CompetitionTeam(
    val id: Id,
    val sessionId: CommonSession.Id,
    val name: String,
    val numberInGame: Int,
    val captain: CompetitionPlayer.TeamCaptain,
    val teamMembers: List<CompetitionPlayer.TeamMember>,
    val banRoundNumber: Int?,
    val password: String,
    val strategy: String?,
) {
    data class Id(val number: Long) {
        override fun toString() = number.toString()
    }
}

val CompetitionTeam.students: List<CompetitionPlayer.Student>
    get() = teamMembers + captain

