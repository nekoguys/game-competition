package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.competition.model.DbCompetitionProcessInfo
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeam
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeamBan
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeamMember
import ru.nekoguys.game.persistence.competition.repository.*
import ru.nekoguys.game.persistence.session.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.session.repository.DbGameSessionsRepository
import ru.nekoguys.game.persistence.user.repository.DbUserRepository

@GamePersistenceTest
internal class DbCompetitionTeamRepositoryTest @Autowired constructor(
    private val dbCompetitionProcessInfoRepository: DbCompetitionProcessInfoRepository,
    private val userRepository: DbUserRepository,
    private val gameSessionsRepository: DbGameSessionsRepository,
    private val gamePropertiesRepository: DbGamePropertiesRepository,
    private val gameStateRepository: DbCompetitionStateRepository,
    private val competitionTeamRepository: DbCompetitionTeamRepository,
    private val competitionTeamBanRepository: DbCompetitionTeamBanRepository,
    private val competitionTeamMemberRepository: DbCompetitionTeamMemberRepository,
) {

    @Test
    fun `team, team members and bans insertion and retrieval`(): Unit = runBlocking {
        val processInfo = processInfo("sample_email")

        val team = DbCompetitionTeam(
            gameId = processInfo.gameId,
            teamNumber = 0,
        ).let { competitionTeamRepository.save(it) }

        val teamMembers: List<DbCompetitionTeamMember> =
            userRepository
                .findAll()
                .withIndex()
                .map {
                    DbCompetitionTeamMember(
                        teamId = team.teamId!!,
                        memberId = it.value.id!!,
                        captain = it.index == 0,
                    )
                }
                .let { competitionTeamMemberRepository.saveAll(it) }
                .toList()

        val bannedTeam = DbCompetitionTeamBan.newTeamBan(
            teamId = team.teamId!!,
            banRound = 2,
        ).let { competitionTeamBanRepository.save(it) }

        assertThat(bannedTeam.isNewTeam).isTrue
        bannedTeam.isNewTeam = false

        val retrievedTeamMembers =
            competitionTeamMemberRepository
                .findAllByTeamId(team.teamId!!)
                .toList()
        assertThat(competitionTeamBanRepository.findById(bannedTeam.teamId))
            .isEqualTo(bannedTeam)
        assertThat(retrievedTeamMembers)
            .containsExactlyInAnyOrderElementsOf(teamMembers)
    }

    private suspend fun processInfo(userEmail: String): DbCompetitionProcessInfo {
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
