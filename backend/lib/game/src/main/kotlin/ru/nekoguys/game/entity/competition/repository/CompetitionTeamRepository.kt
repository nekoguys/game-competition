package ru.nekoguys.game.entity.competition.repository

import kotlinx.coroutines.flow.Flow
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam

interface CompetitionTeamRepository {
    suspend fun create(
        creator: CompetitionPlayer.Unknown,
        name: String,
        password: String,
    ): CompetitionTeam

    suspend fun update(
        from: CompetitionTeam,
        to: CompetitionTeam,
    ) {
        update(listOf(from to to))
    }

    suspend fun update(
        teamsDiff: Collection<Pair<CompetitionTeam, CompetitionTeam>>,
    )

    fun findAllByIds(
        ids: Collection<Long>
    ): Flow<CompetitionTeam>

    suspend fun findByName(
        sessionId: CommonSession.Id,
        teamName: String,
    ): CompetitionTeam?

    fun findBySessionId(
        sessionId: Long,
    ): Flow<CompetitionTeam>

    suspend fun findAllBySessionIds(
        sessionIds: Iterable<Long>,
    ): Map<CommonSession.Id, List<CompetitionTeam>>

    suspend fun findAllTeamIdsBySessionIds(
        sessionIds: Collection<Long>,
    ): Map<CommonSession.Id, List<CompetitionTeam.Id>>

    suspend fun load(
        teamId: CompetitionTeam.Id,
    ): CompetitionTeam
}
