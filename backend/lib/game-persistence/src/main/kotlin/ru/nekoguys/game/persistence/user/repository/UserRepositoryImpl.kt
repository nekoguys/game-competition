package ru.nekoguys.game.persistence.user.repository

import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.toDbUser
import ru.nekoguys.game.persistence.user.model.toDbUserRole
import ru.nekoguys.game.persistence.user.model.toUserOrNull

@Repository
class UserRepositoryImpl(
    private val dbUserRepository: DbUserRepository,
) : UserRepository {
    override suspend fun create(
        email: String,
        password: String,
        role: UserRole,
    ): User {
        val dbUser = DbUser(
            id = null,
            email = email,
            password = password,
            role = role.toDbUserRole(),
        )

        return dbUserRepository.save(dbUser)
            .toUserOrNull()
            ?: error("User created successfully, but it can't be parsed into core model: $dbUser")
    }

    override suspend fun updateUser(user: User) {
        dbUserRepository.save(user.toDbUser())
    }

    override suspend fun deleteUser(userId: User.Id) {
        dbUserRepository.deleteById(userId.number)
    }

    override suspend fun find(userId: Long): User? =
        dbUserRepository
            .findById(userId)
            ?.let { it.toUserOrNull() ?: error("Can't parse role for user: $it") }

    override suspend fun findByEmail(email: String): User? =
        dbUserRepository
            .findByEmail(email)
            ?.let { it.toUserOrNull() ?: error("Can't parse role for user: $it") }
}

