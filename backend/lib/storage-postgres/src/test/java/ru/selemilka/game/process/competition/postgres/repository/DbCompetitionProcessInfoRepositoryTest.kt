package ru.selemilka.game.process.competition.postgres.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.reactive.TransactionalOperator
import ru.selemilka.game.TestingR2dbcRepositoriesConfig
import ru.selemilka.game.game_props.postgres.model.DbGameProperties
import ru.selemilka.game.game_props.postgres.repository.DbGamePropertiesRepository
import ru.selemilka.game.game_sessions.postgres.model.DbGameSession
import ru.selemilka.game.game_sessions.postgres.repository.DbGameSessionsRepository
import ru.selemilka.game.game_states.competition.postgres.model.DbCompetitionState
import ru.selemilka.game.game_states.competition.postgres.repository.DbCompetitionStateRepository
import ru.selemilka.game.process.competition.postgres.model.DbCompetitionProcessInfo
import ru.selemilka.game.process.competition.postgres.model.DbCompetitionRoundAnswer
import ru.selemilka.game.process.competition.postgres.model.DbCompetitionRoundInfo
import ru.selemilka.game.process.competition.postgres.model.DbCompetitionRoundResult
import ru.selemilka.game.teams.competition.postgres.model.DbCompetitionTeam
import ru.selemilka.game.teams.competition.postgres.repository.DbCompetitionTeamRepository
import ru.selemilka.game.user.postgres.model.DbUser
import ru.selemilka.game.user.postgres.model.DbUserRole
import ru.selemilka.game.user.postgres.repository.DbUserRepository
import ru.selemilka.game.user.postgres.repository.runBlockingWithRollback
import java.time.Instant

@DataR2dbcTest
@ContextConfiguration(classes = [TestingR2dbcRepositoriesConfig::class])
internal class DbCompetitionProcessInfoRepositoryTest(
    @Autowired
    val dbCompetitionProcessInfoRepository: DbCompetitionProcessInfoRepository,
    @Autowired
    val dbCompetitionRoundInfoRepository: DbCompetitionRoundInfoRepository,
    @Autowired
    val userRepository: DbUserRepository,
    @Autowired
    val gameSessionsRepository: DbGameSessionsRepository,
    @Autowired
    val gamePropertiesRepository: DbGamePropertiesRepository,
    @Autowired
    val gameStateRepository: DbCompetitionStateRepository,
    @Autowired
    val competitionTeamRepository: DbCompetitionTeamRepository,
    @Autowired
    val roundAnswerRepository: DbCompetitionRoundAnswerRepository,
    @Autowired
    val teamRoundResultRepository: DbCompetitionRoundResultRepository,
    @Autowired
    val transactionalOperator: TransactionalOperator,
) {
    @Test
    fun `process info insertion and retrieval`() {
        val processInfo = processInfo("email")

        val retrievedProcessInfo = runBlocking { dbCompetitionProcessInfoRepository.findById(processInfo.id!!) }
        assertEquals(processInfo, retrievedProcessInfo)
        val roundInfo = runBlockingWithRollback(transactionalOperator) {
            dbCompetitionRoundInfoRepository.save(DbCompetitionRoundInfo(null, processInfo.id!!, 1, Instant.now(), null))
        }
        val retrievedRoundInfo = runBlocking { dbCompetitionRoundInfoRepository.findById(roundInfo.id!!) }
        assertEquals(roundInfo, retrievedRoundInfo)
    }

    @Test
    fun `check answers submission`() {
        val processInfo = processInfo("email")
        val roundInfo = runBlockingWithRollback(transactionalOperator) {
            dbCompetitionRoundInfoRepository.save(DbCompetitionRoundInfo(null, processInfo.id!!, 1, Instant.now(), null))
        }
        val team = runBlockingWithRollback(transactionalOperator) {
            competitionTeamRepository.save(DbCompetitionTeam(processInfo.gameId, 1))
        }
        val answer = runBlockingWithRollback(transactionalOperator) {
            roundAnswerRepository.save(DbCompetitionRoundAnswer(roundInfo.id!!, team.teamId!!, 10))
        }
        val teamRoundResult = runBlockingWithRollback(transactionalOperator) {
            teamRoundResultRepository.save(DbCompetitionRoundResult(roundInfo.id!!, team.teamId!!, 20.0))
        }

        val retrievedAnswer = runBlocking { roundAnswerRepository.findById(answer.id!!) }
        val retrievedTeamRoundResult = runBlocking { teamRoundResultRepository.findById(teamRoundResult.id!!) }
        assertEquals(retrievedAnswer, answer)
        assertEquals(retrievedTeamRoundResult, teamRoundResult)
    }

    private fun processInfo(userEmail: String) : DbCompetitionProcessInfo {
        val processInfo = runBlockingWithRollback(transactionalOperator) {
            val user = userRepository.save(DbUser(null, userEmail, DbUserRole.TEACHER))
            val props = gamePropertiesRepository.save(DbGameProperties(user.id!!, "competition", null))
            val session = gameSessionsRepository.save(DbGameSession(props.id!!))
            val state = gameStateRepository.findFirstByState(DbCompetitionState.State.DRAFT)
            dbCompetitionProcessInfoRepository.save(DbCompetitionProcessInfo(null, session.id!!, state!!.id!!))
        }
        return processInfo
    }
}