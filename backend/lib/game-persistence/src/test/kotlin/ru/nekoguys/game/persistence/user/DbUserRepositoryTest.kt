package ru.nekoguys.game.persistence.user

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.reactive.TransactionalOperator
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository
import ru.nekoguys.game.persistence.utils.runBlockingWithRollback

@GamePersistenceTest
internal class DbUserRepositoryTest @Autowired constructor(
    private val transactionalOperator: TransactionalOperator,
    private val userRepository: DbUserRepository,
) {

    @Test
    fun `simple insertion and retrieval`() = transactionalOperator.runBlockingWithRollback {
        val expectedUser = DbUser(
            id = null,
            email = "kpbenua@edu.hse.ru",
            password = "qwerty",
            role = DbUserRole.ADMIN,
            firstName = null,
            secondName = null,
        ).let { userRepository.save(it) }

        assertThat(userRepository.findById(expectedUser.id!!))
            .isEqualTo(expectedUser)
    }
}
