package ru.nekoguys.game.persistence.commongame.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.commongame.model.DbSessionMessage

interface DbSessionMessageRepository : CoroutineCrudRepository<DbSessionMessage, Long> {
    @Query(
        "SELECT * " +
                "FROM game_session_logs " +
                "WHERE session_id = :sessionId ORDER BY seq_num"
    )
    fun getAllBySessionId(sessionId: Long): Flow<DbSessionMessage>

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    suspend fun countBySessionId(sessionId: Long): Long
}
