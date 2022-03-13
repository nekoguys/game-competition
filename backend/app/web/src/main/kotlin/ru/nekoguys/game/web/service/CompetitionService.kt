package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.commongame.service.pin
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.CompetitionProcessService
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.findAll
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionCreateTeamMessage
import ru.nekoguys.game.entity.competition.rule.CompetitionJoinTeamMessage
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.*
import java.time.LocalDateTime

@Service
class CompetitionService(
    private val competitionProcessService: CompetitionProcessService,
    private val sessionPinDecoder: SessionPinDecoder,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val userRepository: UserRepository,
) {
    suspend fun create(
        userEmail: String,
        request: CreateCompetitionRequest,
    ): CreateCompetitionResponse {
        val user = userRepository.findByEmail(userEmail)
        checkNotNull(user)

        val session = competitionSessionRepository.create(
            userId = user.id,
            settings = request.extractCompetitionSettings(),
            stage = request.state.toCompetitionStage(),
        )

        return if (request.state?.lowercase() == "registration") {
            CreateCompetitionResponse.OpenedRegistration(session.pin)
        } else {
            CreateCompetitionResponse.Created
        }
    }

    suspend fun getCompetitionHistory(
        userEmail: String,
        limit: Int,
        offset: Int,
    ): List<GetCompetitionResponse> {
        val user = userRepository.findByEmail(userEmail)
        checkNotNull(user)

        val sessionIds = competitionSessionRepository
            .findIdsByCreatorId(user.id.number, limit, offset)
            .toList()

        return competitionSessionRepository
            .findAll(
                ids = sessionIds.map { it.number },
                CompetitionSession.WithSettings,
                CompetitionSession.WithStage,
                CompetitionSession.WithCommonFields,
            )
            .map {
                createCompetitionHistoryResponseItem(
                    settings = it.settings,
                    stage = it.stage,
                    lastModified = it.lastModified,
                    isOwned = true,
                    pin = it.pin
                )
            }
            .toList()
    }

    suspend fun createTeam(
        studentEmail: String,
        request: CreateTeamRequest,
    ): CreateTeamResponse {
        require(request.teamName.length >= 4) {
            return CreateTeamResponse.IncorrectName
        }

        val captain = userRepository
            .findByEmail(studentEmail)
            ?: error("No such user: $studentEmail")

        val sessionId = sessionPinDecoder
            .decodeIdFromPin(request.gameId)
            ?: return CreateTeamResponse.GameNotFound(request.gameId)

        return try {
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = captain,
                command = CompetitionCommand.CreateTeam(
                    teamName = request.teamName,
                    password = request.password,
                ),
            )
            CreateTeamResponse.Success
        } catch (ex: CompetitionProcessException) {
            CreateTeamResponse.ProcessError(ex.message)
        }
    }

    suspend fun joinTeam(
        studentEmail: String,
        request: JoinTeamRequest,
    ): JoinTeamResponse {
        val captain = userRepository
            .findByEmail(studentEmail)
            ?: error("No such user: studentEmail")

        val sessionId = sessionPinDecoder
            .decodeIdFromPin(request.competitionPin)
            ?: return JoinTeamResponse.GameNotFound(request.competitionPin)

        return try {
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = captain,
                command = CompetitionCommand.JoinTeam(
                    teamName = request.teamName,
                ),
            )
            JoinTeamResponse.Success(request.teamName)
        } catch (ex: CompetitionProcessException) {
            JoinTeamResponse.ProcessError(ex.message)
        }
    }

    fun teamJoinMessageFlow(
        userEmail: String,
        sessionPin: String,
    ): Flow<TeamUpdateNotification> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        competitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body } // не смотрим, каким командам отправлено сообщение
            .transform { msg ->
                when (msg) {
                    is CompetitionCreateTeamMessage ->
                        emit(msg.toUpdateNotification())
                    is CompetitionJoinTeamMessage ->
                        emit(msg.toUpdateNotification())
                    else -> Unit
                }
            }
            .collect(::emit)
    }

    fun myTeamJoinMessageFlow(
        userEmail: String,
        sessionPin: String
    ): Flow<TeamMemberUpdateNotification> = flow {
        val sessionId = sessionPinGenerator
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")
        val session = competitionSessionRepository
            .load(
                sessionId,
                CompetitionSession.WithTeams,
            )
        val team = session.teams.first {
            it.students.any { member -> member.user.email == userEmail }
        }
        competitionProcessService.getAllMessagesForSession(sessionId)
            .map { it.body }
            .transform { msg ->
                val notification = when (msg) {
                    is CompetitionCreateTeamMessage -> Pair(msg.teamName == team.name, msg.toNewTeamMemberNotification())
                    is CompetitionJoinTeamMessage -> Pair(msg.teamName == team.name, msg.toNewTeamMemberNotification())
                    else -> return@transform
                }
                emit(notification)
            }
            .mapNotNull { if (it.first) it.second else null }
            .collect(::emit)
    }

    suspend fun ifSessionCanBeJoined(
        sessionPin: String,
    ): Boolean {
        val sessionId = sessionPinDecoder
            .decodeIdFromPinUnsafe(sessionPin)
            ?: return false

        val session = competitionSessionRepository
            .findAll(listOf(sessionId), CompetitionSession.WithStage)
            .singleOrNull() ?: return false

        return session.stage == CompetitionStage.Registration
    }

    suspend fun getCompetitionCloneInfo(sessionPin: String): CompetitionCloneInfoResponse? {
        val id = sessionPinDecoder
            .decodeIdFromPinUnsafe(sessionPin)
            ?: return null

        return competitionSessionRepository
            .findAll(listOf(id), CompetitionSession.Full)
            .singleOrNull()
            ?.toCompetitionCloneInfo()
    }
}

private fun CreateCompetitionRequest.extractCompetitionSettings() =
    CompetitionSettings(
        name = name,
        expensesFormula = expensesFormula.toCompetitionExpensesFormula(),
        demandFormula = demandFormula.toCompetitionDemandFormula(),
        maxTeamsAmount = maxTeamsAmount!!,
        maxTeamSize = maxTeamSize!!,
        roundsCount = roundsCount!!,
        roundLength = roundLength!!,
        teamLossLimit = teamLossUpperbound!!,
        instruction = instruction!!,
        showPreviousRoundResults = shouldShowStudentPreviousRoundResults!!,
        endRoundBeforeAllAnswered = shouldEndRoundBeforeAllAnswered ?: false,
        showStudentsResultsTable = shouldShowResultTableInEnd ?: false,
        isAutoRoundEnding = isAutoRoundEnding ?: false,
        showOtherTeamsMembers = showOtherTeamsMembers ?: false,
    )

private fun String?.toCompetitionStage(): CompetitionStage =
    when (val processedStage = this?.trim()?.lowercase()) {
        CompetitionStage.Draft.name.lowercase() ->
            CompetitionStage.Draft
        CompetitionStage.Registration.name.lowercase() ->
            CompetitionStage.Registration
        else -> error("Unknown or unsupported stage $processedStage")
    }

private fun List<Double>.toCompetitionExpensesFormula() =
    CompetitionExpensesFormula(
        xSquareCoefficient = get(0),
        xCoefficient = get(1),
        freeCoefficient = get(2),
    )

private fun List<Double>.toCompetitionDemandFormula() =
    CompetitionDemandFormula(
        freeCoefficient = get(0),
        xCoefficient = get(1),
    )

private val CompetitionSettings.demandFormulaString: String
    get() = listOf(
        demandFormula.freeCoefficient.toString(),
        demandFormula.xCoefficient.toString(),
    ).joinToString(";")

private val CompetitionSettings.expensesFormulaString: String
    get() = listOf(
        expensesFormula.xSquareCoefficient.toString(),
        expensesFormula.xCoefficient.toString(),
        expensesFormula.freeCoefficient.toString(),
    ).joinToString(";")

private fun createCompetitionHistoryResponseItem(
    settings: CompetitionSettings,
    stage: CompetitionStage,
    lastModified: LocalDateTime,
    @Suppress("SameParameterValue") isOwned: Boolean,
    pin: String,
) =
    GetCompetitionResponse(
        demandFormula = settings.demandFormulaString,
        expensesFormula = settings.expensesFormulaString,
        instruction = settings.instruction,
        isAutoRoundEnding = settings.isAutoRoundEnding,
        isOwned = isOwned,
        lastUpdateTime = lastModified,
        maxTeamSize = settings.maxTeamSize,
        maxTeamsAmount = settings.maxTeamsAmount,
        name = settings.name,
        pin = pin,
        roundLength = settings.roundLength,
        roundsCount = settings.roundsCount,
        shouldEndRoundBeforeAllAnswered = settings.endRoundBeforeAllAnswered,
        shouldShowResultTableInEnd = settings.showStudentsResultsTable,
        shouldShowStudentPreviousRoundResults = settings.showPreviousRoundResults,
        showOtherTeamsMembers = settings.showOtherTeamsMembers,
        state = stage.name,
        teamLossUpperbound = settings.teamLossLimit.toDouble(),
    )

private fun CompetitionCreateTeamMessage.toUpdateNotification() =
    TeamUpdateNotification(
        teamName = teamName,
        idInGame = idInGame,
        teamMembers = listOf(captainEmail)
    )

private fun CompetitionJoinTeamMessage.toUpdateNotification() =
    TeamUpdateNotification(
        teamName = teamName,
        idInGame = idInGame,
        teamMembers = membersEmails,
    )

private fun CompetitionCreateTeamMessage.toNewTeamMemberNotification() =
    TeamMemberUpdateNotification(
        name = captainEmail,
        isCaptain = true
    )

private fun CompetitionJoinTeamMessage.toNewTeamMemberNotification() =
    TeamMemberUpdateNotification(
        name = membersEmails.last(),
        isCaptain = false
    )

private fun CompetitionSession.Full.toCompetitionCloneInfo() =
    CompetitionCloneInfoResponse(
        name = settings.name,
        expensesFormula = settings.expensesFormulaString,
        demandFormula = settings.demandFormulaString,
        instruction = settings.instruction,
        isAutoRoundEnding = settings.isAutoRoundEnding,
        maxTeamSize = settings.maxTeamSize,
        maxTeamsAmount = settings.maxTeamsAmount,
        roundLength = settings.roundLength,
        roundsCount = settings.roundsCount,
        shouldShowResultsTableInEnd = settings.showStudentsResultsTable,
        shouldShowStudentPreviousRoundResults = settings.showPreviousRoundResults,
        showOtherTeamsMembers = settings.showOtherTeamsMembers,
        teamLossUpperbound = settings.teamLossLimit.toDouble()
    )
