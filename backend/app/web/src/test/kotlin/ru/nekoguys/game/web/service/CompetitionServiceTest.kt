package ru.nekoguys.game.web.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.CreateCompetitionRequest
import ru.nekoguys.game.web.dto.CreateCompetitionResponse
import ru.nekoguys.game.web.dto.CreateTeamRequest
import ru.nekoguys.game.web.dto.CreateTeamResponse

@GameWebApplicationTest
class CompetitionServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val competitionService: CompetitionService,
) {

    private lateinit var teacher: User
    private lateinit var student: User

    init {
        runBlocking {
            teacher = userRepository.create(
                email = "teacher1@hse.ru",
                password = "898",
                role = UserRole.Teacher
            )

            student = userRepository.create(
                email = "student1@edu.hse.ru",
                password = "898",
                role = UserRole.Student
            )
        }
    }

    @Test
    fun `can create competition in draft state`(): Unit = runBlocking {
        val result = competitionService.create(
            userEmail = teacher.email,
            request = DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST,
        )

        assertThat(result)
            .isEqualTo(CreateCompetitionResponse.Created)
    }

    @Test
    fun `can create a team`(): Unit = runBlocking {
        val createCompetitionResult = competitionService.create(
            userEmail = teacher.email,
            request = DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST
                .copy(state = "Registration"),
        ) as CreateCompetitionResponse.CreatedRegistered
        val competitionPin = createCompetitionResult.pin

        val result = competitionService.createTeam(
            studentEmail = student.email,
            request = CreateTeamRequest(
                pin = competitionPin,
                teamName = "Test team",
                captainEmail = student.email,
                password = "password"
            )
        )

        assertThat(result)
            .isEqualTo(CreateTeamResponse.Success)
    }
}

val DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST = CreateCompetitionRequest(
    demandFormula = listOf(-2.0, 4.0),
    expensesFormula = listOf(1.0, -3.0, -2.0),
    instruction = "Test instruction",
    isAutoRoundEnding = true,
    maxTeamSize = 5,
    maxTeamsAmount = 10,
    name = "Test competition",
    roundLength = 15,
    roundsCount = 3,
    shouldEndRoundBeforeAllAnswered = true,
    shouldShowResultTableInEnd = true,
    shouldShowStudentPreviousRoundResults = true,
    showOtherTeamsMembers = true,
    state = "DRAFT",
    teamLossUpperbound = 1000,
)
