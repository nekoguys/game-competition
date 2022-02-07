package ru.nekoguys.game.persistence.commongame.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import ru.nekoguys.game.persistence.commongame.model.DbGameSession

interface DbGameSessionRepository : CoroutineCrudRepository<DbGameSession, Long> {
    fun findAllByPropertiesIdIn(
        propertiesId: Collection<Long>,
    ): Flow<DbGameSession>

    @Query("""
        SELECT s.id, s.props_id, s.last_modified_date
        FROM game_sessions AS s
        JOIN game_props AS p ON p.id = s.props_id
        WHERE p.creator_id = :creatorId
        ORDER BY s.last_modified_date DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findAllByCreatorId(
        creatorId: Long,
        limit: Int,
        offset: Int,
    ): Flow<DbGameSession>

    @Query("""
        SELECT EXISTS (
            SELECT gp.*
            FROM game_props AS gp
            JOIN game_sessions AS gs ON gs.props_id = gp.id
            WHERE gp.creator_id = :creatorId AND gs.id = :sessionId
        )
    """)
    suspend fun existsByIdAndCreatorId(
        sessionId: Long,
        creatorId: Long,
    ): Boolean
}
