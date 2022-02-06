package ru.nekoguys.game.web.util

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import ru.nekoguys.game.entity.commongame.service.SessionPinGenerator
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.CreateCompetitionRequest
import ru.nekoguys.game.web.dto.CreateTeamRequest
import ru.nekoguys.game.web.dto.JoinTeamRequest
import ru.nekoguys.game.web.service.CompetitionService
import java.util.concurrent.atomic.AtomicLong

@Component
class TestUtils(
    private val competitionService: CompetitionService,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val pinGenerator: SessionPinGenerator,
    private val transactionalOperator: TransactionalOperator,
    private val userRepository: UserRepository,
) {

    private val indexCounter = AtomicLong()

    private fun nextIndex() = indexCounter.incrementAndGet()

    fun runBlockingWithRollback(block: suspend () -> Unit): Unit =
        runBlocking {
            transactionalOperator.executeAndAwait { tx ->
                tx.setRollbackOnly()
                block()
            }
        }

    suspend fun createUser(
        role: UserRole = UserRole.Admin,
        email: String = "${role.toString().lowercase()}-${nextIndex()}@hse.ru",
        password: String = DEFAULT_PASSWORD,
    ): User =
        userRepository.create(email, password, role)

    suspend fun createAndLoadCompetition(
        teacher: User? = null,
        request: CreateCompetitionRequest,
    ): CompetitionSession {
        val createdTeacher = teacher ?: createUser()
        competitionService.create(createdTeacher.email, request)

        return competitionSessionRepository
            .findByCreatorId(createdTeacher.id.number)
            .first { it.properties.settings.name == request.name }
    }

    suspend fun createDraftCompetition(
        user: User? = null,
        request: CreateCompetitionRequest = DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST,
    ): String =
        createAndLoadCompetition(user, request)
            .let { pinGenerator.convertSessionIdToPin(it.id) }

    suspend fun createCompetition(
        teacher: User? = null,
        request: CreateCompetitionRequest = DEFAULT_CREATE_COMPETITION_REQUEST,
    ): String =
        createAndLoadCompetition(teacher, request)
            .let { pinGenerator.convertSessionIdToPin(it.id) }

    suspend fun createTeam(
        captain: User? = null,
        competitionPin: String? = null,
        teamName: String? = null,
        password: String = DEFAULT_PASSWORD,
    ): TestCreateTeamResult {
        val createdCaptain = captain ?: createUser(role = UserRole.Student)

        val request = CreateTeamRequest(
            pin = competitionPin ?: createCompetition(),
            teamName = teamName ?: "Team ${nextIndex()}",
            captainEmail = createdCaptain.email,
            password = password,
        )
        competitionService.createTeam(createdCaptain.email, request)

        return TestCreateTeamResult(
            competitionPin = request.pin,
            teamName = request.teamName,
            password = request.password,
        )
    }

    data class TestCreateTeamResult(
        val competitionPin: String,
        val teamName: String,
        val password: String,
    )

    suspend fun joinTeam(
        teamMate: User? = null,
        competitionPin: String? = null,
        teamName: String? = null,
        password: String = DEFAULT_PASSWORD,
    ): TestJoinTeamResult {
        require(competitionPin != null || teamName == null)

        val createdTeamMate = teamMate ?: createUser(role = UserRole.Student)
        val createdCompetitionPin = competitionPin ?: createCompetition()
        val createdTeamName = teamName
            ?: createTeam(competitionPin = createdCompetitionPin).teamName

        val request = JoinTeamRequest(
            competitionPin = createdCompetitionPin,
            teamName = createdTeamName,
            password = password,
        )

        competitionService.joinTeam(createdTeamMate.email, request)

        return TestJoinTeamResult(
            competitionPin = request.competitionPin,
            teamName = request.teamName,
            password = request.password,
        )
    }

    data class TestJoinTeamResult(
        val competitionPin: String,
        val teamName: String,
        val password: String,
    )

    companion object TestData {
        const val DEFAULT_PASSWORD = "password"

        val DEFAULT_CREATE_COMPETITION_REQUEST = CreateCompetitionRequest(
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
            state = "Registration",
            teamLossUpperbound = 1000,
        )

        val DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST =
            DEFAULT_CREATE_COMPETITION_REQUEST.copy(state = "Draft")
    }
}
