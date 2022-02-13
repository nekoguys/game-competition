package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeamMember

interface DbCompetitionTeamMemberRepository : CoroutineCrudRepository<DbCompetitionTeamMember, Long> {
    @Suppress("SpringDataRepositoryMethodParametersInspection")
    fun findAllByTeamId(teamId: Long): Flow<DbCompetitionTeamMember>

    @Query(
        "SELECT ctm.* " +
                "FROM competition_team_members AS ctm " +
                "JOIN competition_teams AS ct ON ct.id = ctm.team_id " +
                "WHERE ct.session_id = :sessionId AND ctm.user_id = :userId " +
                "LIMIT 1"
    )
    suspend fun findBySessionIdAndUserId(
        sessionId: Long,
        userId: Long,
    ): DbCompetitionTeamMember?

    @Query(
        "SELECT ctm.* " +
                "FROM competition_team_members AS ctm " +
                "JOIN competition_teams AS ct ON ct.id = ctm.team_id " +
                "WHERE ct.session_id IN (:sessionIds)"
    )
    fun findAllBySessionIds(
        sessionIds: Iterable<Long>,
    ): Flow<DbCompetitionTeamMember>

    @Suppress("SpringDataRepositoryMethodParametersInspection")
    suspend fun countByTeamIdAndIdLessThanEqual(
        teamId: Long,
        id: Long,
    ): Int
}
