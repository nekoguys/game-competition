package ru.nekoguys.game.persistence.competition

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.reactive.TransactionalOperator
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.commongame.model.DbGameSession
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.*
import ru.nekoguys.game.persistence.competition.repository.*
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository
import ru.nekoguys.game.persistence.utils.runBlockingWithRollback
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@GamePersistenceTest
internal class DbCompetitionSessionRepositoryTest @Autowired constructor(
    private val dbCompetitionRoundAnswerRepository: DbCompetitionRoundAnswerRepository,
    private val dbCompetitionRoundInfoRepository: DbCompetitionRoundInfoRepository,
    private val dbCompetitionRoundResultRepository: DbCompetitionRoundResultRepository,
    private val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
    private val dbCompetitionTeamRepository: DbCompetitionTeamRepository,
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

    @Test
    fun `round info insertion and retrieval`() = transactionalOperator.runBlockingWithRollback {
        val competitionSession = createCompetitionSession("email")

        val roundInfo = DbCompetitionRoundInfo(
            id = null,
            sessionId = competitionSession.sessionId!!,
            roundNumber = 1,
            startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            endTime = null,
        ).let { dbCompetitionRoundInfoRepository.save(it) }

        val retrievedRoundInfo = dbCompetitionRoundInfoRepository.findById(roundInfo.id!!)

        assertThat(retrievedRoundInfo)
            .isEqualTo(roundInfo)
    }

    @Test
    fun `check answers submission`() = transactionalOperator.runBlockingWithRollback {
        val competitionSession = createCompetitionSession("email")

        val roundInfo = DbCompetitionRoundInfo(
            id = null,
            sessionId = competitionSession.sessionId!!,
            roundNumber = 1,
            startTime = LocalDateTime.now(),
            endTime = null,
        ).let { dbCompetitionRoundInfoRepository.save(it) }

        val team = DbCompetitionTeam(
            id = null,
            sessionId = competitionSession.sessionId!!,
            teamNumber = 1,
            name = "Test Team",
            password = "a",
            banRound = null,
        ).let { dbCompetitionTeamRepository.save(it) }

        val answer = DbCompetitionRoundAnswer(
            roundInfoId = roundInfo.id!!,
            teamId = team.id!!,
            value = 10,
        ).let { dbCompetitionRoundAnswerRepository.save(it) }

        val teamRoundResult = DbCompetitionRoundResult(
            roundInfoId = roundInfo.id!!,
            teamId = team.id!!,
            income = 20.0,
        ).let { dbCompetitionRoundResultRepository.save(it) }

        assertThat(dbCompetitionRoundAnswerRepository.findById(answer.id!!))
            .isEqualTo(answer)

        assertThat(dbCompetitionRoundResultRepository.findById(teamRoundResult.id!!))
            .isEqualTo(teamRoundResult)
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
