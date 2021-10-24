package ru.selemilka.game.user.storage

import org.springframework.stereotype.Component
import ru.selemilka.game.user.model.User

@Component
class UserStorage {
    suspend fun getUserById(id: Long): User? {
        TODO()
    }
}