package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.persistence.GamePersistenceTest

@GamePersistenceTest
class CompetitionSessionRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val competitionSessionRepository: CompetitionSessionRepository,
) {
    private val user: User = runBlocking {
        userRepository.create(
            email = "test@hse.ru",
            password = "880",
            role = UserRole.Teacher,
        )
    }

    @Test
    fun `can create`(): Unit = runBlocking {
        val createdSession = competitionSessionRepository.create(
            userId = user.id,
            settings = DEFAULT_COMPETITION_SETTINGS,
            stage = CompetitionStage.InProgress(round = 4)
        )

        val expectedSession = CompetitionSession(
            id = createdSession.id,
            properties = CompetitionProperties(
                id = createdSession.properties.id,
                settings = DEFAULT_COMPETITION_SETTINGS,
                creatorId = user.id,
            ),
            stage = CompetitionStage.InProgress(round = 4),
            lastModified = createdSession.lastModified,
            teams = emptyList(),
        )

        val actualSession = competitionSessionRepository.load(createdSession.id)

        assertThat(actualSession)
            .usingRecursiveComparison()
            .withStrictTypeChecking()
            .isEqualTo(expectedSession)
    }
}
