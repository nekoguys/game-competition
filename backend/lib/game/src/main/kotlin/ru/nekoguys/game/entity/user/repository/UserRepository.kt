package ru.nekoguys.game.entity.user.repository

import kotlinx.coroutines.flow.Flow
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole

interface UserRepository {
    suspend fun create(
        email: String,
        password: String,
        role: UserRole,
    ): User

    fun findAll(userIds: Iterable<Long>): Flow<User>

    suspend fun find(userId: Long): User?

    suspend fun findByEmail(email: String): User?

    suspend fun updateUser(user: User)

    suspend fun deleteUser(userId: User.Id)
}

suspend fun UserRepository.load(userId: User.Id): User =
    find(userId.number) ?: error("User with ID $userId doesn't exist")
