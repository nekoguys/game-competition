package ru.nekoguys.game.web.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.CreateCompetitionRequest
import ru.nekoguys.game.web.dto.CreateCompetitionResponse

@GameWebApplicationTest
class CompetitionServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val competitionService: CompetitionService,
) {

    private val user = runBlocking {
        userRepository.create(
            email = "test@hse.ru",
            password = "898",
            role = UserRole.Teacher
        )
    }

    @Test
    fun `can create competition in draft state`(): Unit = runBlocking {
        val result = competitionService.create(
            userEmail = user.email,
            request = DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST,
        )

        assertThat(result)
            .isEqualTo(CreateCompetitionResponse.Created)
    }
}

val DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST = CreateCompetitionRequest(
    demandFormula = listOf("-2.0", "4.0"),
    expensesFormula = listOf("1.0", "-3.0", "-2.0"),
    instruction = "Test instruction",
    isAutoRoundEnding = true,
    maxTeamSize = 5,
    maxTeamsAmount = 10,
    name = "Test competition",
    roundLength = 0,
    roundsCount = 0,
    shouldEndRoundBeforeAllAnswered = true,
    shouldShowResultTableInEnd = true,
    shouldShowStudentPreviousRoundResults = true,
    showOtherTeamsMembers = true,
    state = "DRAFT",
    teamLossUpperbound = 1000,
)
