package ru.nekoguys.game.persistence.commongame.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import ru.nekoguys.game.persistence.commongame.model.DbGameProperties

interface DbGamePropertiesRepository : CoroutineSortingRepository<DbGameProperties, Long> {
    @Query("""
        SELECT *
        FROM game_props AS gp
        JOIN game_sessions AS gs ON gs.props_id = gp.id
        WHERE gs.id = :sessionId
        LIMIT 1
    """)
    suspend fun findBySessionId(
        sessionId: Long,
    ): DbGameProperties?

    fun findAllByIdIn(
        ids: Collection<Long>,
        sort: Sort = Sort.by(DbGameProperties::id.name),
    ): Flow<DbGameProperties>

    fun findAllByCreatorId(
        creatorId: Long,
        page: Pageable,
    ): Flow<DbGameProperties>
}
