package ru.nekoguys.game.web.util

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.commongame.service.toPin
import ru.nekoguys.game.entity.competition.model.CompetitionSession
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
import java.util.concurrent.atomic.AtomicLong

@Component
class TestGame(
    private val competitionService: CompetitionService,
    private val competitionProcessService: CompetitionProcessService,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val pinGenerator: SessionPinDecoder,
    private val userRepository: UserRepository,
) {
    private fun nextIndex() = indexCounter.incrementAndGet()

    private fun nextEmail(role: UserRole) =
        "${role.toString().lowercase()}-${nextIndex()}@hse.ru"

    private fun nextCompetitionName() =
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
        userRepository.updateUser(user.copy(
            firstName = firstName,
            secondName = secondName,
        ))
        userRepository.findByEmail(email)!!
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
        request: CreateCompetitionRequest = DEFAULT_CREATE_COMPETITION_REQUEST
            .copy(name = nextCompetitionName()),
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

    fun createCompetition(
        teacher: User = createUser(UserRole.Teacher),
        request: CreateCompetitionRequest = DEFAULT_CREATE_COMPETITION_REQUEST
            .copy(name = nextCompetitionName()),
    ): String =
        createAndLoadCompetition(teacher, request)
            .id
            .toPin()

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
            check(response is CreateTeamResponse.Success)
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
            check(response is JoinTeamResponse.Success)
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

    fun startCompetition(
        competitionPin: String = createCompetition(),
    ): Unit = runBlocking {
        val teacherId = loadCompetitionSession(competitionPin).creatorId
        val teacher = userRepository.load(teacherId)
        val response = competitionProcessService
            .startCompetition(teacher.email, competitionPin)
        check(response == StartCompetitionResponse.Success)
    }

    companion object TestData {
        private val indexCounter = AtomicLong()

        const val DEFAULT_ADMIN_EMAIL = "admin@hse.ru"
        const val DEFAULT_TEACHER_EMAIL = "teacher@hse.ru"
        const val DEFAULT_STUDENT_EMAIL = "student@edu.hse.ru"
        const val DEFAULT_PASSWORD = "password"
        const val DEFAULT_COMPETITION_NAME = "Test Competition"

        val DEFAULT_CREATE_COMPETITION_REQUEST = CreateCompetitionRequest(
            demandFormula = listOf(-2.0, 4.0),
            expensesFormula = listOf(1.0, -3.0, -2.0),
            instruction = "Test instruction",
            isAutoRoundEnding = true,
            maxTeamSize = 2,
            maxTeamsAmount = 4,
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
