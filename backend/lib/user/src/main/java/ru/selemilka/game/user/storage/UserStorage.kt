package ru.selemilka.game.user.storage

import org.springframework.stereotype.Component
import ru.selemilka.game.user.model.User
import ru.selemilka.game.user.postgres.repository.DbUserRepository

@Component
class UserStorage(
    val dbUserRepository: DbUserRepository
) {
    suspend fun getUserById(id: Long): User? {
        TODO()
    }
}