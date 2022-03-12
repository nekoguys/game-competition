package ru.nekoguys.game.web.util

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.springframework.stereotype.Component
import ru.nekoguys.game.entity.commongame.service.SessionPinGenerator
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.findAll
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionService
import java.util.concurrent.atomic.AtomicLong

@Component
class TestGame(
    private val competitionService: CompetitionService,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val pinGenerator: SessionPinGenerator,
    private val userRepository: UserRepository,
) {
    private fun nextIndex() = indexCounter.incrementAndGet()

    private fun nextEmail(role: UserRole) =
        "${role.toString().lowercase()}-${nextIndex()}@hse.ru"

    private fun nextTeamName() = "Team ${nextIndex()}"

    fun createUser(
        role: UserRole = UserRole.Admin,
        email: String = nextEmail(role),
        password: String = DEFAULT_PASSWORD,
    ): User = runBlocking {
        userRepository.create(email, "{noop}$password", role)
    }

    fun loadCompetitionSession(
        pin: String = createCompetition(),
    ): CompetitionSession.Full = runBlocking {
        val sessionId = pinGenerator.decodeIdFromPin(pin)
        competitionSessionRepository
            .load(sessionId!!, CompetitionSession.Full)
    }

    fun createAndLoadCompetition(
        teacher: User = createUser(UserRole.Teacher),
        request: CreateCompetitionRequest = DEFAULT_CREATE_COMPETITION_REQUEST,
    ): CompetitionSession.Full = runBlocking {
        competitionService.create(teacher.email, request)

        val sessionIds = competitionSessionRepository
            .findIdsByCreatorId(teacher.id.number)
            .map { it.number }

        competitionSessionRepository
            .findAll(sessionIds, CompetitionSession.Full)
            .first { it.settings.name == request.name }
    }

    fun createCompetition(
        teacher: User = createUser(UserRole.Teacher),
        request: CreateCompetitionRequest = DEFAULT_CREATE_COMPETITION_REQUEST,
    ): String =
        createAndLoadCompetition(teacher, request)
            .let { pinGenerator.convertSessionIdToPin(it.id) }

    fun createTeam(
        competitionPin: String = createCompetition(),
        captain: User = createUser(UserRole.Student),
        teamName: String = nextTeamName(),
        password: String = DEFAULT_PASSWORD,
    ): TestCreateTeamResult {
        val request = CreateTeamRequest(
            gameId = competitionPin,
            teamName = teamName,
            captainEmail = captain.email,
            password = password,
        )

        runBlocking {
            val response = competitionService.createTeam(captain.email, request)
            assertThat(response)
                .isEqualTo(CreateTeamResponse.Success)
        }

        return TestCreateTeamResult(
            competitionPin = request.gameId,
            teamName = request.teamName,
            password = request.password,
        )
    }

    data class TestCreateTeamResult(
        val competitionPin: String,
        val teamName: String,
        val password: String,
    )

    fun joinTeam(
        competitionPin: String = createCompetition(),
        teamName: String = createTeam(competitionPin = competitionPin).teamName,
        teamMember: User = createUser(role = UserRole.Student),
        password: String = DEFAULT_PASSWORD,
    ): TestCreateTeamResult {
        val request = JoinTeamRequest(
            competitionPin = competitionPin,
            teamName = teamName,
            password = password,
        )

        runBlocking {
            val response = competitionService.joinTeam(teamMember.email, request)
            assertThat(response)
                .isInstanceOf(JoinTeamResponse.Success::class.java)
        }

        return TestCreateTeamResult(
            competitionPin = request.competitionPin,
            teamName = request.teamName,
            password = request.password,
        )
    }

    /**
     * Псевдоним для [joinTeam]
     *
     * С ним тесты иногда проще читать
     */
    fun createAndJoinTeam(
        competitionPin: String = createCompetition(),
        teamMember: User = createUser(role = UserRole.Student),
    ): TestCreateTeamResult = runBlocking {
        joinTeam(competitionPin, teamMember = teamMember)
    }

    companion object TestData {
        private val indexCounter = AtomicLong()

        const val DEFAULT_EMAIL = "test@hse.ru"
        const val DEFAULT_PASSWORD = "password"
        const val DEFAULT_COMPETITION_NAME = "Test Competition"

        val DEFAULT_CREATE_COMPETITION_REQUEST = CreateCompetitionRequest(
            demandFormula = listOf(-2.0, 4.0),
            expensesFormula = listOf(1.0, -3.0, -2.0),
            instruction = "Test instruction",
            isAutoRoundEnding = true,
            maxTeamSize = 5,
            maxTeamsAmount = 10,
            name = DEFAULT_COMPETITION_NAME,
            roundLength = 15,
            roundsCount = 3,
            shouldEndRoundBeforeAllAnswered = true,
            shouldShowResultTableInEnd = true,
            shouldShowStudentPreviousRoundResults = true,
            showOtherTeamsMembers = true,
            state = "Registration",
            teamLossUpperbound = 1000,
        )

        val DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST =
            DEFAULT_CREATE_COMPETITION_REQUEST.copy(
                state = "Draft",
            )
    }
}
