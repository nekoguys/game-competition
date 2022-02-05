package ru.nekoguys.game.entity.competition.repository

import kotlinx.coroutines.flow.Flow
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam

interface CompetitionTeamRepository {
    suspend fun create(
        creator: CompetitionPlayer.Unknown,
        name: String,
        maxTeams: Int,
    ): CompetitionTeam

    suspend fun findByName(
        sessionId: CommonSession.Id,
        teamName: String,
    ): CompetitionTeam?

    fun loadBySession(
        sessionId: CommonSession.Id,
    ): Flow<CompetitionTeam>

    fun loadAllBySession(
        sessionIds: Iterable<CommonSession.Id>,
    ): Flow<CompetitionTeam>
}
