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
        FROM groudina.public.game_sessions AS s
        JOIN groudina.public.game_props AS p ON p.id = s.props_id
        WHERE p.creator_id = :creatorId
        ORDER BY s.last_modified_date DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findAllByCreatorId(
        @Param("creatorId") creatorId: Long,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): Flow<DbGameSession>
}
