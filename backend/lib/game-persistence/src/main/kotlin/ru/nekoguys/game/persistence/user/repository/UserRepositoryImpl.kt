package ru.nekoguys.game.persistence.user.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java)

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
            firstName = null,
            secondName = null,
        )

        return dbUserRepository.save(dbUser)
            .toUserOrNull()
            ?.also { logger.info("Saved user $it to DB") }
            ?: error("User created successfully, but it can't be parsed into core model: $dbUser")
    }

    override fun findAll(userIds: Iterable<Long>): Flow<User> =
        dbUserRepository
            .findAllById(userIds)
            .map {
                it.toUserOrNull() ?: error("Can't parse role for user: $it")
            }
            .also { logger.info("Loading user with ids $userIds from DB") }

    override suspend fun find(userId: Long): User? =
        dbUserRepository
            .findById(userId)
            ?.also { logger.info("Loaded user with id $userId from DB") }
            ?.let { it.toUserOrNull() ?: error("Can't parse role for user: $it") }

    override suspend fun findByEmail(email: String): User? =
        dbUserRepository
            .findByEmail(email)
            ?.let { it.toUserOrNull() ?: error("Can't parse role for user: $it") }

    override suspend fun updateUser(user: User) {
        dbUserRepository.save(user.toDbUser())
        logger.info("Saved user $user to DB")
    }

    override suspend fun deleteUser(userId: User.Id) {
        dbUserRepository.deleteById(userId.number)
        logger.info("Deleted user with id $userId from DB")
    }
}

