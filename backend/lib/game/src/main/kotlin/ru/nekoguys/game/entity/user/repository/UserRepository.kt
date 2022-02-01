package ru.nekoguys.game.entity.user.repository

import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole

interface UserRepository {
    suspend fun create(
        email: String,
        password: String,
        role: UserRole,
    ): User

    suspend fun load(userId: Long): User?

    suspend fun findByEmail(email: String): User?

    suspend fun load(userId: User.Id): User

    suspend fun updateUser(user: User)

    suspend fun deleteUser(userId: User.Id)
}
