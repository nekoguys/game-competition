package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.commongame.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionSession
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeam
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeamBan
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeamMember
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionSessionRepository
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionTeamBanRepository
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionTeamMemberRepository
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionTeamRepository
import ru.nekoguys.game.persistence.user.repository.DbUserRepository

@GamePersistenceTest
internal class DbCompetitionTeamRepositoryTest @Autowired constructor(
    private val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
    private val dbCompetitionTeamBanRepository: DbCompetitionTeamBanRepository,
    private val dbCompetitionTeamMemberRepository: DbCompetitionTeamMemberRepository,
    private val dbCompetitionTeamRepository: DbCompetitionTeamRepository,
    private val dbGamePropertiesRepository: DbGamePropertiesRepository,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbUserRepository: DbUserRepository,
) {
    @Test
    fun `team, team members and bans insertion and retrieval`(): Unit = runBlocking {
        val processInfo = competitionSession("sample_email")

        val team = DbCompetitionTeam(
            gameId = processInfo.parentId!!,
            teamNumber = 0,
        ).let { dbCompetitionTeamRepository.save(it) }

        val teamMembers: List<DbCompetitionTeamMember> =
            dbUserRepository
                .findAll()
                .withIndex()
                .map {
                    DbCompetitionTeamMember(
                        teamId = team.teamId!!,
                        memberId = it.value.id!!,
                        captain = it.index == 0,
                    )
                }
                .let { dbCompetitionTeamMemberRepository.saveAll(it) }
                .toList()

        val bannedTeam = DbCompetitionTeamBan(
            teamId = team.teamId!!,
            banRound = 2,
        ).let { dbCompetitionTeamBanRepository.save(it) }

        val retrievedTeamMembers =
            dbCompetitionTeamMemberRepository
                .findAllByTeamId(team.teamId!!)
                .toList()
        assertThat(dbCompetitionTeamBanRepository.findById(bannedTeam.teamId))
            .isEqualTo(bannedTeam)
        assertThat(retrievedTeamMembers)
            .containsExactlyInAnyOrderElementsOf(teamMembers)
    }

    private suspend fun competitionSession(userEmail: String): DbCompetitionSession {
        return createCompetitionSession(
            userEmail,
            dbUserRepository,
            dbGamePropertiesRepository,
            dbGameSessionRepository,
            dbCompetitionSessionRepository,
        )
    }
}
