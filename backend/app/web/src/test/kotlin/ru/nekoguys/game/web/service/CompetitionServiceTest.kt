package ru.nekoguys.game.web.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.util.TestUtils

@GameWebApplicationTest
class CompetitionServiceTest @Autowired constructor(
    private val competitionService: CompetitionService,
    private val testUtils: TestUtils,
) {

    @Test
    fun `can create competition in draft state`() = testUtils.runBlockingWithRollback {
        val teacher = testUtils.createUser()

        val result = competitionService.create(
            userEmail = teacher.email,
            request = TestUtils.DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST,
        )

        assertThat(result)
            .isEqualTo(CreateCompetitionResponse.Created)
    }

    @Test
    fun `can create competition in registration state`() = testUtils.runBlockingWithRollback {
        val teacher = testUtils.createUser()

        val result = competitionService.create(
            userEmail = teacher.email,
            request = TestUtils.DEFAULT_CREATE_COMPETITION_REQUEST,
        )

        assertThat(result)
            .isInstanceOf(CreateCompetitionResponse.OpenedRegistration::class.java)
    }

    @Test
    fun `can create a team`() = testUtils.runBlockingWithRollback {
        val competitionPin = testUtils.createCompetition()
        val captain = testUtils.createUser(role = UserRole.Student)

        val createTeamResult = competitionService.createTeam(
            studentEmail = captain.email,
            request = CreateTeamRequest(
                pin = competitionPin,
                teamName = "Test team",
                captainEmail = captain.email,
                password = "password"
            )
        )

        assertThat(createTeamResult)
            .isEqualTo(CreateTeamResponse.Success)
    }

    @Test
    fun `can't create a team twice`() = testUtils.runBlockingWithRollback {
        val competitionPin = testUtils.createCompetition()
        val captain = testUtils.createUser(role = UserRole.Student)
        testUtils.createTeam(captain, competitionPin)

        val createOtherTeamResult = competitionService.createTeam(
            studentEmail = captain.email,
            request = CreateTeamRequest(
                pin = competitionPin,
                teamName = "Test team",
                captainEmail = captain.email,
                password = "password"
            )
        )

        assertThat(createOtherTeamResult)
            .isInstanceOf(CreateTeamResponse.ProcessError::class.java)
    }

    @Test
    fun `can join a team`() = testUtils.runBlockingWithRollback {
        val (competitionPin, teamName) = testUtils.createTeam()
        val teamMate = testUtils.createUser()

        val joinTeamResult = competitionService.joinTeam(
            studentEmail = teamMate.email,
            request = JoinTeamRequest(
                competitionPin = competitionPin,
                teamName = teamName,
                password = TestUtils.DEFAULT_PASSWORD
            )
        )

        assertThat(joinTeamResult)
            .isEqualTo(JoinTeamResponse.Success(teamName))
    }

    @Test
    fun `can't join a team twice`() = testUtils.runBlockingWithRollback {
        val student = testUtils.createUser()
        val (competitionPin, teamName) = testUtils.joinTeam(teamMate = student)

        val joinTeamResult = competitionService.joinTeam(
            studentEmail = student.email,
            request = JoinTeamRequest(
                competitionPin = competitionPin,
                teamName = teamName,
                password = TestUtils.DEFAULT_PASSWORD
            )
        )

        assertThat(joinTeamResult)
            .isNotInstanceOf(JoinTeamResponse.Success::class.java)
    }
}
