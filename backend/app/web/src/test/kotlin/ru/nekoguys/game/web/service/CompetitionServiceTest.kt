package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.commongame.service.pin
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.TeamMemberUpdateNotification
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import java.time.LocalDateTime

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class CompetitionServiceTest @Autowired constructor(
    private val game: TestGame,
    private val competitionService: CompetitionService,
) {

    @Test
    fun `can create competition in draft state`() {
        val session = game.createAndLoadSession(
            request = TestGame.DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST,
        )

        with(session) {
            assertThat(settings.name).isNotNull
            assertThat(lastModified).isBefore(LocalDateTime.now())
            assertThat(stage).isEqualTo(CompetitionStage.Draft)
            assertThat(teams).isEmpty()
        }
    }

    @Test
    fun `can create competition in registration state`() {
        val session = game.createAndLoadSession(
            request = TestGame.DEFAULT_CREATE_COMPETITION_REQUEST,
        )

        with(session) {
            assertThat(settings.name).isNotNull
            assertThat(lastModified).isBefore(LocalDateTime.now())
            assertThat(stage).isEqualTo(CompetitionStage.Registration)
            assertThat(teams).isEmpty()
        }
    }

    @Test
    fun `can create a team`() {
        val sessionPin = game.createSession()
        val captain = game.createUser(role = UserRole.Student)
        game.createTeam(sessionPin = sessionPin, captain = captain)

        val session = game.loadSession(sessionPin)

        assertThat(session.teams).hasSize(1)
        assertThat(session.teams[0].captain.user).isEqualTo(captain)
    }

    @Test
    fun `valid my team members flow`() {
        val competitionPin = game.createCompetition()
        val captain = game.createUser(role = UserRole.Student)
        val sampleMember = game.createUser(role = UserRole.Student)

        val captain2 = game.createUser(role = UserRole.Student)
        game.createTeam(competitionPin = competitionPin, captain = captain2)
        val team = game.createTeam(competitionPin = competitionPin, captain = captain)
        game.joinTeam(competitionPin, team.teamName, sampleMember, team.password)

        fun checkValidFlow(flow: Flow<TeamMemberUpdateNotification>) {
            runBlocking {
                val flowResult = flow.take(2).toList()
                assertThat(flowResult[0].name).isEqualTo(captain.email)
                assertThat(flowResult[0].isCaptain).isTrue()
                assertThat(flowResult[1].name).isEqualTo(sampleMember.email)
            }
        }
        checkValidFlow(competitionService.myTeamJoinMessageFlow(captain.email, competitionPin))
        checkValidFlow(competitionService.myTeamJoinMessageFlow(sampleMember.email, competitionPin))
    }

    @Test
    fun `get clone info for competition`() {
        val session = game.createAndLoadSession()
        val settings = session.settings

        val cloneInfo = runBlocking {
            competitionService.getCompetitionCloneInfo(session.pin)
        }

        assertThat(cloneInfo)
            .usingRecursiveComparison()
            .ignoringExpectedNullFields()
            .isEqualTo(@Suppress("unused") object {
                val name = settings.name
                val instruction = settings.instruction
                val maxTeamSize = settings.maxTeamSize
                val maxTeamsAmount = settings.maxTeamsAmount
                val shouldShowResultsTableInEnd = settings.showStudentsResultsTable
                val shouldShowStudentPreviousRoundResults = settings.showPreviousRoundResults
                val roundLength = settings.roundLength
                val isAutoRoundEnding = settings.isAutoRoundEnding
            })
    }
}
