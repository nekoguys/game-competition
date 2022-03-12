package ru.nekoguys.game.persistence.commongame.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.commongame.model.DbSessionMessage

interface DbSessionMessageRepository : CoroutineCrudRepository<DbSessionMessage, Long> {
    fun getAllBySessionId(sessionId: Long): Flow<DbSessionMessage>

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    suspend fun countBySessionId(sessionId: Long): Long
}
