package ru.nekoguys.game.persistence.user.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.nekoguys.game.persistence.user.model.DbUser

@Repository
interface DbUserRepository : CoroutineCrudRepository<DbUser, Long> {
    fun findAllByEmail(email: String): Flow<DbUser>
    suspend fun findByEmail(email: String): DbUser?
}
