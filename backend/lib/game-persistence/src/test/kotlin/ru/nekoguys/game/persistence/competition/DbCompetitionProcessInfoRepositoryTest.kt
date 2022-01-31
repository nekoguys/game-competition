package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.competition.model.*
import ru.nekoguys.game.persistence.competition.repository.*
import ru.nekoguys.game.persistence.session.model.DbGameProperties
import ru.nekoguys.game.persistence.session.model.DbGameSession
import ru.nekoguys.game.persistence.session.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.session.repository.DbGameSessionsRepository
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@GamePersistenceTest
internal class DbCompetitionProcessInfoRepositoryTest @Autowired constructor(
    private val dbCompetitionProcessInfoRepository: DbCompetitionProcessInfoRepository,
    private val dbCompetitionRoundInfoRepository: DbCompetitionRoundInfoRepository,
    private val userRepository: DbUserRepository,
    private val gameSessionsRepository: DbGameSessionsRepository,
    private val gamePropertiesRepository: DbGamePropertiesRepository,
    private val gameStateRepository: DbCompetitionStateRepository,
    private val competitionTeamRepository: DbCompetitionTeamRepository,
    private val roundAnswerRepository: DbCompetitionRoundAnswerRepository,
    private val teamRoundResultRepository: DbCompetitionRoundResultRepository,
) {
    @Test
    fun `process info insertion and retrieval`(): Unit = runBlocking {
        val processInfo = createProcessInfo("email")

        val retrievedProcessInfo = dbCompetitionProcessInfoRepository.findById(processInfo.id!!)

        assertThat(retrievedProcessInfo)
            .isEqualTo(retrievedProcessInfo)
    }

    @Test
    fun `round info insertion and retrieval`(): Unit = runBlocking {
        val processInfo = createProcessInfo("email")

        val roundInfo = DbCompetitionRoundInfo(
            id = null,
            processId = processInfo.id!!,
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
        val processInfo = createProcessInfo("email")

        val roundInfo = DbCompetitionRoundInfo(
            id = null,
            processId = processInfo.id!!,
            roundNumber = 1,
            startTime = LocalDateTime.now(),
            endTime = null,
        ).let { dbCompetitionRoundInfoRepository.save(it) }

        val team = DbCompetitionTeam(
            gameId = processInfo.gameId,
            teamNumber = 1,
        ).let { competitionTeamRepository.save(it) }

        val answer = DbCompetitionRoundAnswer(
            roundInfoId = roundInfo.id!!,
            teamId = team.teamId!!,
            value = 10,
        ).let { roundAnswerRepository.save(it) }

        val teamRoundResult = DbCompetitionRoundResult(
            roundInfoId = roundInfo.id!!,
            teamId = team.teamId!!,
            income = 20.0,
        ).let { teamRoundResultRepository.save(it) }

        assertThat(roundAnswerRepository.findById(answer.id!!))
            .isEqualTo(answer)

        assertThat(teamRoundResultRepository.findById(teamRoundResult.id!!))
            .isEqualTo(teamRoundResult)
    }

    private suspend fun createProcessInfo(userEmail: String): DbCompetitionProcessInfo {
        return createProcessInfo(
            userEmail,
            userRepository,
            gamePropertiesRepository,
            gameSessionsRepository,
            gameStateRepository,
            dbCompetitionProcessInfoRepository
        )
    }
}

suspend fun createProcessInfo(
    userEmail: String,
    userRepository: DbUserRepository,
    gamePropertiesRepository: DbGamePropertiesRepository,
    gameSessionsRepository: DbGameSessionsRepository,
    gameStateRepository: DbCompetitionStateRepository,
    competitionProcessInfoRepository: DbCompetitionProcessInfoRepository,
): DbCompetitionProcessInfo {

    val user = DbUser(
        id = null,
        email = userEmail,
        password = "qwerty",
        role = DbUserRole.TEACHER,
    ).let { userRepository.save(it) }

    val props = DbGameProperties(
        creatorId = user.id!!,
        gameType = "competition",
        competitionPropsId = null,
    ).let { gamePropertiesRepository.save(it) }

    val session = DbGameSession(propertiesId = props.id!!)
        .let { gameSessionsRepository.save(it) }

    val state = gameStateRepository.findFirstByState(DbCompetitionState.State.DRAFT)

    return competitionProcessInfoRepository.save(DbCompetitionProcessInfo(
        null,
        session.id!!,
        state!!.id!!))
}
