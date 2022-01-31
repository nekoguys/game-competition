package ru.nekoguys.game.persistence.user

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository

@GamePersistenceTest
internal class DbUserRepositoryTest @Autowired constructor(
    private val userRepository: DbUserRepository
) {

    @Test
    fun `simple insertion and retrieval`(): Unit = runBlocking {
        val expectedUser = DbUser(
            email = "kpbenua@edu.hse.ru",
            password = "qwerty",
            role = DbUserRole.ADMIN,
        ).let { userRepository.save(it) }

        assertThat(userRepository.findById(expectedUser.id!!))
            .isEqualTo(expectedUser)
    }
}
