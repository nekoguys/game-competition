package ru.nekoguys.game.persistence.competition

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.reactive.TransactionalOperator
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.utils.runBlockingWithRollback

@GamePersistenceTest
class CompetitionSessionRepositoryTest @Autowired constructor(
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val transactionalOperator: TransactionalOperator,
    private val userRepository: UserRepository,
) {
    @Test
    fun `can create`() = transactionalOperator.runBlockingWithRollback {
        val user = userRepository.create(
            email = "test@hse.ru",
            password = "880",
            role = UserRole.Teacher,
        )

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
