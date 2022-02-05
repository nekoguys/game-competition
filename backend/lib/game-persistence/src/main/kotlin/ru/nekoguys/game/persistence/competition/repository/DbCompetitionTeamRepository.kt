package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeam

interface DbCompetitionTeamRepository
    : CoroutineCrudRepository<DbCompetitionTeam, Long> {

    fun findAllBySessionId(
        sessionId: Long,
    ): Flow<DbCompetitionTeam>

    @Suppress("SpringDataRepositoryMethodParametersInspection")
    suspend fun findBySessionIdAndName(
        sessionId: Long,
        name: String,
    ): DbCompetitionTeam?

    @Suppress("SpringDataRepositoryMethodParametersInspection")
    suspend fun countBySessionIdAndIdLessThanEqual(
        sessionId: Long,
        id: Long,
    ): Int
}
