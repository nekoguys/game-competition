package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.commongame.model.DbGameProperties
import ru.nekoguys.game.persistence.commongame.model.DbGameSession
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.*
import ru.nekoguys.game.persistence.competition.repository.*
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@GamePersistenceTest
internal class DbCompetitionSessionRepositoryTest @Autowired constructor(
    private val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
    private val dbCompetitionRoundAnswerRepository: DbCompetitionRoundAnswerRepository,
    private val dbCompetitionRoundInfoRepository: DbCompetitionRoundInfoRepository,
    private val dbCompetitionRoundResultRepository: DbCompetitionRoundResultRepository,
    private val dbCompetitionTeamRepository: DbCompetitionTeamRepository,
    private val dbGamePropertiesRepository: DbGamePropertiesRepository,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbUserRepository: DbUserRepository,
) {
    @Test
    fun `session insertion and retrieval`(): Unit = runBlocking {
        val createdSession = createCompetitionSession("email")

        val retrievedSession = dbCompetitionSessionRepository.findById(createdSession.parentId!!)

        assertThat(retrievedSession)
            .isEqualTo(createdSession)
    }

    @Test
    fun `round info insertion and retrieval`(): Unit = runBlocking {
        val competitionSession = createCompetitionSession("email")

        val roundInfo = DbCompetitionRoundInfo(
            id = null,
            sessionId = competitionSession.parentId!!,
            roundNumber = 1,
            startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            endTime = null,
        ).let { dbCompetitionRoundInfoRepository.save(it) }

        val retrievedRoundInfo = dbCompetitionRoundInfoRepository.findById(roundInfo.id!!)

        assertThat(retrievedRoundInfo)
            .isEqualTo(roundInfo)
    }

    @Test
    fun `check answers submission`(): Unit = runBlocking {
        val competitionSession = createCompetitionSession("email")

        val roundInfo = DbCompetitionRoundInfo(
            id = null,
            sessionId = competitionSession.parentId!!,
            roundNumber = 1,
            startTime = LocalDateTime.now(),
            endTime = null,
        ).let { dbCompetitionRoundInfoRepository.save(it) }

        val team = DbCompetitionTeam(
            id = null,
            sessionId = competitionSession.parentId!!,
            teamNumber = 1,
            name = "Test Team",
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
            dbGamePropertiesRepository,
            dbGameSessionRepository,
            dbCompetitionSessionRepository,
        )
    }
}

suspend fun createCompetitionSession(
    userEmail: String,
    userRepository: DbUserRepository,
    gamePropertiesRepository: DbGamePropertiesRepository,
    gameSessionsRepository: DbGameSessionRepository,
    gameStateRepository: DbCompetitionSessionRepository,
): DbCompetitionSession {

    val user = DbUser(
        id = null,
        email = userEmail,
        password = "qwerty",
        role = DbUserRole.TEACHER,
    ).let { userRepository.save(it) }

    val props = DbGameProperties(
        id = null,
        creatorId = user.id!!,
        gameType = DbGameType.COMPETITION,
    ).let { gamePropertiesRepository.save(it) }

    val session = DbGameSession(
        id = null,
        propertiesId = props.id!!,
    ).let { gameSessionsRepository.save(it) }

    return DbCompetitionSession(
        parentId = session.id!!,
        stage = DbCompetitionStage.IN_PROGRESS,
        lastRound = 4,
    ).let { gameStateRepository.save(it.asNew()) }
}
