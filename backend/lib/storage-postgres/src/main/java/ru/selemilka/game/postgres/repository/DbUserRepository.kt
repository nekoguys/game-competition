package ru.selemilka.game.postgres.repository

import org.springframework.stereotype.Component
import ru.selemilka.game.postgres.model.DbUserRole
import ru.selemilka.game.postgres.model.DbUser

@Component
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