package ru.selemilka.game.user.postgres.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.selemilka.game.user.postgres.model.DbUser

interface DbUserRepository : CoroutineCrudRepository<DbUser, Long> {
    fun findAllByEmail(email: String): Flow<DbUser>
}
