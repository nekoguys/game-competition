package ru.nekoguys.game.persistence.commongame.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.commongame.model.DbGameSession

interface DbGameSessionRepository : CoroutineCrudRepository<DbGameSession, Long> {
    @Query(
        """
        SELECT s.id
        FROM game_sessions AS s
        WHERE s.creator_id = :creatorId
        ORDER BY s.last_modified_date DESC
        LIMIT :limit OFFSET :offset
    """
    )
    fun findIdsByCreatorId(
        creatorId: Long,
        limit: Int,
        offset: Int,
    ): Flow<Long>

    @Suppress(
        "SpringDataRepositoryMethodParametersInspection",
        "SpringDataRepositoryMethodReturnTypeInspection",
    )
    suspend fun existsByIdAndCreatorId(
        id: Long,
        creatorId: Long,
    ): Boolean
}
