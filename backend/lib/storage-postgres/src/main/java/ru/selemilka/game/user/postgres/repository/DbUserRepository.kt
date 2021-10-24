package ru.selemilka.game.user.postgres.repository

import org.springframework.stereotype.Repository
import ru.selemilka.game.user.postgres.model.DbUser
import ru.selemilka.game.user.postgres.model.DbUserRole

@Repository
class DbUserRepository {
    suspend fun findUserById(id: Long): DbUser? {
        return DbUser(
            id = 42,
            email = "mock@hse.ru",
            name = "Mock Mockovich",
            role = DbUserRole.ADMIN,
        )
    }
}