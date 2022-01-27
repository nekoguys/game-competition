package ru.nekoguys.game.persistence.session

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.session.model.DbGameProperties
import ru.nekoguys.game.persistence.session.model.DbGameSession
import ru.nekoguys.game.persistence.session.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.session.repository.DbGameSessionsRepository
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository

@GamePersistenceTest
internal class DbGameSessionRepositoryTest @Autowired constructor(
    private val gameSessionsRepository: DbGameSessionsRepository,
    private val userRepository: DbUserRepository,
    private val gamePropertiesRepository: DbGamePropertiesRepository,
) {
    @Test
    fun `insert and retrieval`(): Unit = runBlocking {
        val user = DbUser(
            email = "email",
            role = DbUserRole.TEACHER,
        ).let { userRepository.save(it) }

        val properties = DbGameProperties(
            creatorId = user.id!!,
            gameType = "competition",
            competitionPropsId = null
        ).let { gamePropertiesRepository.save(it) }

        val gameSession = DbGameSession(properties.id!!)
            .let { gameSessionsRepository.save(it) }

        assertThat(gameSessionsRepository.findById(gameSession.id!!))
            .isEqualTo(gameSession)
    }
}
