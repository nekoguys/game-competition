package ru.nekoguys.game.web.util

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.springframework.stereotype.Component
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.commongame.service.toPin
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.findAll
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.entity.user.repository.load
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionProcessService
import ru.nekoguys.game.web.service.CompetitionService
import ru.nekoguys.game.web.service.CompetitionTeacherProcessService
import ru.nekoguys.game.web.service.CompetitionTeamService
import java.util.concurrent.atomic.AtomicLong

@Component
class TestGame(
    private val competitionService: CompetitionService,
    private val competitionTeamService: CompetitionTeamService,
    private val competitionTeacherProcessService: CompetitionTeacherProcessService,
    private val competitionProcessService: CompetitionProcessService,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val pinGenerator: SessionPinDecoder,
    private val userRepository: UserRepository,
) {
    private fun nextIndex() = indexCounter.incrementAndGet()

    private fun nextEmail(role: UserRole) =
        "${role.toString().lowercase()}-${nextIndex()}@hse.ru"

    private fun nextSessionName() =
        "Competition ${nextIndex()}"

    private fun nextTeamName() = "Team ${nextIndex()}"

    fun createUser(
        role: UserRole = UserRole.Admin,
        email: String = nextEmail(role),
        password: String = DEFAULT_PASSWORD,
        firstName: String? = null,
        secondName: String? = null,
    ): User = runBlocking {
        val user = userRepository.create(email, "{noop}$password", role)
        userRepository.updateUser(
            user.copy(
                firstName = firstName,
                secondName = secondName,
            )
        )
        userRepository.findByEmail(email)!!
    }

    fun loadSession(
        pin: String = createSession(),
    ): CompetitionSession.Full = runBlocking {
        val sessionId = pinGenerator.decodeIdFromPin(pin)
        competitionSessionRepository
            .load(sessionId!!, CompetitionSession.Full)
    }

    fun createAndLoadSession(
        teacher: User = createUser(UserRole.Teacher),
        request: CreateCompetitionRequest = DEFAULT_CREATE_COMPETITION_REQUEST
            .copy(name = nextSessionName()),
        additionalActions: suspend (String) -> Unit = {},
    ): CompetitionSession.Full = runBlocking {
        competitionService.create(teacher.email, request)

        val sessionIdsOfTeacher = competitionSessionRepository
            .findIdsByCreatorId(teacher.id.number)
            .map { it.number }

        val sessionId = competitionSessionRepository
            .findAll(sessionIdsOfTeacher, CompetitionSession.WithSettings)
            .first { it.settings.name == request.name }
            .id

        additionalActions(sessionId.toPin())

        competitionSessionRepository.load(sessionId, CompetitionSession.Full)
    }

    fun createSession(
        teacher: User = createUser(UserRole.Teacher),
        request: CreateCompetitionRequest = DEFAULT_CREATE_COMPETITION_REQUEST
            .copy(name = nextSessionName()),
        additionalActions: suspend (String) -> Unit = {},
    ): String =
        createAndLoadSession(teacher, request, additionalActions)
            .id
            .toPin()

    fun createTeam(
        sessionPin: String = createSession(),
        captain: User = createUser(UserRole.Student),
        teamName: String = nextTeamName(),
        password: String = DEFAULT_PASSWORD,
    ): TestCreateTeamResult {
        val request = CreateTeamRequest(
            teamName = teamName,
            captainEmail = captain.email,
            password = password,
        )

        runBlocking {
            val response = competitionTeamService.create(sessionPin, captain.email, request)
            assertThat(response).isInstanceOf(CreateTeamResponse.Success::class.java)
        }

        return TestCreateTeamResult(
            sessionPin = sessionPin,
            teamName = request.teamName,
            password = request.password,
        )
    }

    data class TestCreateTeamResult(
        val sessionPin: String,
        val teamName: String,
        val password: String,
    )

    fun joinTeam(
        sessionPin: String = createSession(),
        teamName: String = createTeam(sessionPin = sessionPin).teamName,
        teamMember: User = createUser(role = UserRole.Student),
        password: String = DEFAULT_PASSWORD,
    ): TestCreateTeamResult {
        val request = JoinTeamRequest(
            teamName = teamName,
            password = password,
        )

        runBlocking {
            val response = competitionTeamService.join(sessionPin, teamMember.email, request)
            check(response is JoinTeamResponse.Success)
        }

        return TestCreateTeamResult(
            sessionPin = sessionPin,
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
        sessionPin: String = createSession(),
        teamMember: User = createUser(role = UserRole.Student),
    ): TestCreateTeamResult = runBlocking {
        joinTeam(sessionPin, teamMember = teamMember)
    }

    fun startCompetition(
        sessionPin: String = createSession(),
        vararg captains: User,
    ): String = runBlocking {
        for (captain in captains) {
            createTeam(sessionPin, captain)
        }

        val teacherId = loadSession(sessionPin).creatorId
        val teacher = userRepository.load(teacherId)
        val response = competitionTeacherProcessService
            .startCompetition(teacher.email, sessionPin)
        assertThat(response)
            .usingRecursiveComparison()
            .isEqualTo(StartCompetitionResponse)

        sessionPin
    }

    fun startRound(
        sessionPin: String = startCompetition(),
    ): String = runBlocking {
        val teacherId = loadSession(sessionPin).creatorId
        val teacher = userRepository.load(teacherId)
        val response = competitionTeacherProcessService
            .startRound(teacher.email, sessionPin)
        assertThat(response)
            .usingRecursiveComparison()
            .isEqualTo(StartRoundResponse)
        sessionPin
    }

    fun submitAnswer(
        sessionPin: String = createSession(),
        captain: User,
        answer: Long,
    ): Unit = runBlocking {
        val session = loadSession(sessionPin)
        val response = competitionProcessService
            .submitAnswer(
                studentEmail = captain.email,
                sessionPin = sessionPin,
                roundNumber = (session.stage as CompetitionStage.InProcess).round,
                answer = answer,
            )
        assertThat(response)
            .usingRecursiveComparison()
            .isEqualTo(SubmitAnswerResponse)
    }

    companion object TestData {
        private val indexCounter = AtomicLong()

        const val DEFAULT_ADMIN_EMAIL = "admin@hse.ru"
        const val DEFAULT_TEACHER_EMAIL = "admin@hse.ru"
        const val DEFAULT_STUDENT_EMAIL = "student@edu.hse.ru"
        const val DEFAULT_PASSWORD = "password"
        const val DEFAULT_COMPETITION_NAME = "Test Competition"

        val DEFAULT_CREATE_COMPETITION_REQUEST = CreateCompetitionRequest(
            demandFormula = listOf(-2.0, 4.0),
            expensesFormula = listOf(1.0, -3.0, -2.0),
            instruction = "Test instruction",
            isAutoRoundEnding = true,
            maxTeamSize = Int.MAX_VALUE,
            maxTeamsAmount = Int.MAX_VALUE,
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
