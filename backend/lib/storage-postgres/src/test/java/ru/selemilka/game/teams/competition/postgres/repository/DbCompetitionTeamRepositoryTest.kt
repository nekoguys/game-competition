package ru.selemilka.game.teams.competition.postgres.repository

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.reactive.TransactionalOperator
import ru.selemilka.game.TestingR2dbcRepositoriesConfig
import ru.selemilka.game.game_props.postgres.repository.DbGamePropertiesRepository
import ru.selemilka.game.game_sessions.postgres.repository.DbGameSessionsRepository
import ru.selemilka.game.game_states.competition.postgres.repository.DbCompetitionStateRepository
import ru.selemilka.game.process.competition.postgres.model.DbCompetitionProcessInfo
import ru.selemilka.game.process.competition.postgres.repository.DbCompetitionProcessInfoRepository
import ru.selemilka.game.process.competition.postgres.repository.createProcessInfo
import ru.selemilka.game.teams.competition.postgres.model.DbCompetitionTeam
import ru.selemilka.game.teams.competition.postgres.model.DbCompetitionTeamBan
import ru.selemilka.game.teams.competition.postgres.model.DbCompetitionTeamMember
import ru.selemilka.game.user.postgres.repository.DbUserRepository
import ru.selemilka.game.user.postgres.repository.runBlockingWithRollback

@DataR2dbcTest
@ContextConfiguration(classes = [TestingR2dbcRepositoriesConfig::class])
internal class DbCompetitionTeamRepositoryTest(
    @Autowired
    val dbCompetitionProcessInfoRepository: DbCompetitionProcessInfoRepository,
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
    val competitionTeamBanRepository: DbCompetitionTeamBanRepository,
    @Autowired
    val competitionTeamMemberRepository: DbCompetitionTeamMemberRepository,
    @Autowired
    val transactionalOperator: TransactionalOperator,
) {

    @Test
    fun `team, team members and bans insertion and retrieval`() {
        val processInfo = processInfo("sample_email")
        val team = runBlockingWithRollback(transactionalOperator) {
            competitionTeamRepository.save(DbCompetitionTeam(processInfo.gameId, 0))
        }
        val teamMembers = runBlockingWithRollback(transactionalOperator) {
            val allUsers = userRepository.findAll()

            competitionTeamMemberRepository.saveAll(
                allUsers.withIndex().map {
                    DbCompetitionTeamMember(team.teamId!!, it.value.id!!, it.index == 0)
                }
            ).toList()
        }
        val bannedTeam = runBlockingWithRollback(transactionalOperator) {
            competitionTeamBanRepository.save(DbCompetitionTeamBan.newTeamBan(team.teamId!!, 2))
        }
        assertEquals(bannedTeam.isNewTeam, true)
        bannedTeam.isNewTeam = false

        val retrievedTeamMembers =
            runBlocking { competitionTeamMemberRepository.findAllByTeamId(team.teamId!!).toList() }
        val retrievedBannedTeam = runBlocking { competitionTeamBanRepository.findById(bannedTeam.teamId) }
        assertEquals(bannedTeam, retrievedBannedTeam)
        assertIterableEquals(teamMembers, retrievedTeamMembers)
    }

    private fun processInfo(userEmail: String): DbCompetitionProcessInfo {
        return createProcessInfo(
            userEmail,
            transactionalOperator,
            userRepository,
            gamePropertiesRepository,
            gameSessionsRepository,
            gameStateRepository,
            dbCompetitionProcessInfoRepository
        )
    }
}