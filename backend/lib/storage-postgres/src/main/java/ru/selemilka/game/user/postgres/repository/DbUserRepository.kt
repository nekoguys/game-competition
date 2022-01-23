package ru.selemilka.game.user.postgres.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.selemilka.game.user.postgres.model.DbUser

@Repository
interface DbUserRepository : CoroutineCrudRepository<DbUser, Long> {
    fun findAllByEmail(email: String): Flow<DbUser>
}
