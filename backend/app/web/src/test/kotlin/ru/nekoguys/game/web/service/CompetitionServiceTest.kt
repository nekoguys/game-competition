package ru.nekoguys.game.web.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.commongame.service.pin
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.CreateCompetitionRequest
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
    fun `get clone info for competition`() {
        val teacher = game.createUser(role = UserRole.Teacher)
        val session = game.createAndLoadSession(teacher = teacher)
        val settings = session.settings

        val cloneInfo = runBlocking {
            competitionService.getCompetitionCloneInfo(teacher.email, session.pin)
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

    @Test
    fun `get competition list`() {
        val testUser = game.createUser(role = UserRole.Teacher)

        repeat(2) {
            game.createAndLoadSession { pin ->
                repeat(1) { game.createTeam(pin) }
            }
        }

        val participatedSessions = List(2) {
            game.createAndLoadSession { pin ->
                repeat(1) { game.createTeam(pin) }
                game.createAndJoinTeam(
                    sessionPin = pin,
                    teamMember = testUser,
                )
            }
        }

        val createdSessions = List(2) {
            game.createAndLoadSession(
                teacher = testUser
            ) { pin ->
                repeat(1) { game.createTeam(pin) }
            }
        }

        val result = runBlocking {
            competitionService.getCompetitionHistory(
                userEmail = testUser.email,
                limit = Int.MAX_VALUE,
                offset = 0,
            )
        }.map { it.pin }

        assertThat(result)
            .containsExactlyElementsOf(
                (participatedSessions + createdSessions)
                    .sortedByDescending { it.lastModified }
                    .map { it.pin }
            )
    }

    @Test
    fun `change competition settings`() {
        val teacher = game.createUser(
            role = UserRole.Teacher
        )
        val session = game.createAndLoadSession(
            teacher = teacher,
            request = TestGame.DEFAULT_CREATE_COMPETITION_REQUEST,
        )
        val newSettings = CreateCompetitionRequest(
            demandFormula = listOf(-2.0, 1337.0),
            expensesFormula = listOf(1.0, -3.0, 1337.0),
            instruction = "Updated instruction",
            isAutoRoundEnding = false,
            maxTeamSize = 4,
            maxTeamsAmount = 4,
            name = "Updated name",
            roundLength = 4,
            roundsCount = 4,
            shouldEndRoundBeforeAllAnswered = false,
            shouldShowResultTableInEnd = false,
            shouldShowStudentPreviousRoundResults = false,
            showOtherTeamsMembers = false,
            state = "Created",
            teamLossUpperbound = 1337,
        ).extractCompetitionSettings()

        val response = runBlocking {
            competitionService.changeCompetitionSettings(
                userEmail = teacher.email,
                sessionPin = session.pin,
                competitionSettings = newSettings,
                state = null
            )
        }

        val savedSettings = game.loadSession(session.pin).settings

        assertThat(savedSettings)
            .usingRecursiveComparison()
            .isEqualTo(newSettings)

    }
}
