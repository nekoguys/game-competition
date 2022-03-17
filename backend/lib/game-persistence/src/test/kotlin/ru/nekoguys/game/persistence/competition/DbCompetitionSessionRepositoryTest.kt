package ru.nekoguys.game.persistence.competition

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.reactive.TransactionalOperator
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.commongame.model.DbGameSession
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionSession
import ru.nekoguys.game.persistence.competition.model.DbCompetitionStage
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionSessionRepository
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository
import ru.nekoguys.game.persistence.utils.runBlockingWithRollback

@GamePersistenceTest
internal class DbCompetitionSessionRepositoryTest @Autowired constructor(
    private val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbUserRepository: DbUserRepository,
    private val transactionalOperator: TransactionalOperator,
) {
    @Test
    fun `session insertion and retrieval`() = transactionalOperator.runBlockingWithRollback {
        val createdSession = createCompetitionSession("email")

        val retrievedSession = dbCompetitionSessionRepository.findById(createdSession.sessionId!!)

        assertThat(retrievedSession)
            .isEqualTo(createdSession)
    }

    private suspend fun createCompetitionSession(userEmail: String): DbCompetitionSession {
        return createCompetitionSession(
            userEmail,
            dbUserRepository,
            dbGameSessionRepository,
            dbCompetitionSessionRepository,
        )
    }
}

suspend fun createCompetitionSession(
    userEmail: String,
    userRepository: DbUserRepository,
    gameSessionsRepository: DbGameSessionRepository,
    gameStateRepository: DbCompetitionSessionRepository,
): DbCompetitionSession {

    val user = DbUser(
        id = null,
        email = userEmail,
        password = "qwerty",
        role = DbUserRole.TEACHER,
        firstName = null,
        secondName = null,
    ).let { userRepository.save(it) }

    val session = DbGameSession(
        id = null,
        creatorId = user.id!!,
        gameType = DbGameType.COMPETITION,
    ).let { gameSessionsRepository.save(it) }

    return DbCompetitionSession(
        sessionId = session.id!!,
        stage = DbCompetitionStage.IN_PROGRESS,
        lastRound = 4,
    ).let { gameStateRepository.save(it.asNew()) }
}
