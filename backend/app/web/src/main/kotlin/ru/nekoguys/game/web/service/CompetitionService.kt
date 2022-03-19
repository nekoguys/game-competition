package ru.nekoguys.game.web.service

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
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.CreateCompetitionRequest
import ru.nekoguys.game.web.dto.CreateCompetitionResponse
import ru.nekoguys.game.web.dto.GetCompetitionResponse
import java.time.LocalDateTime

@Service
class CompetitionService(
    private val sessionPinDecoder: SessionPinDecoder,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val competitionProcessService: CompetitionProcessService,
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

        val sessionIds =
            competitionSessionRepository
                .findIdsByParticipantId(user.id.number, limit, offset)
                .toList()

        return competitionSessionRepository
            .findAll(
                ids = sessionIds.map { it.number },
                CompetitionSession.WithSettings,
                CompetitionSession.WithStage,
                CompetitionSession.WithCommonFields,
            )
            .sortedByDescending { it.lastModified }
            .map {
                createCompetitionHistoryResponseItem(
                    settings = it.settings,
                    stage = it.stage,
                    lastModified = it.lastModified,
                    isOwned = it.creatorId == user.id,
                    pin = it.pin
                )
            }
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

    suspend fun getCompetitionCloneInfo(
        userEmail: String,
        sessionPin: String,
    ): GetCompetitionResponse? {
        val user = userRepository.findByEmail(userEmail) ?: error("can't find user")

        val id = sessionPinDecoder
            .decodeIdFromPinUnsafe(sessionPin)
            ?: return null

        return competitionSessionRepository
            .findAll(listOf(id), CompetitionSession.Full)
            .singleOrNull()
            ?.run {
                createCompetitionHistoryResponseItem(
                    settings = this.settings,
                    stage = this.stage,
                    lastModified = this.lastModified,
                    isOwned = this.creatorId == user.id,
                    pin = this.pin
                )
            }
    }

    suspend fun changeCompetitionSettings(
        userEmail: String,
        sessionPin: String,
        competitionSettings: CompetitionSettings,
    ): GetCompetitionResponse {
        val user = userRepository.findByEmail(userEmail)
        checkNotNull(user)

        return try {
            val sessionId = sessionPinDecoder.decodeIdFromPin(sessionPin)
                ?: error("No competition with pin $sessionPin")
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = user,
                command = CompetitionCommand.ChangeCompetitionSettings(
                    newSettings = competitionSettings
                )
            )

            competitionSessionRepository.load(
                id = sessionId,
                CompetitionSession.Full
            ).run {
                createCompetitionHistoryResponseItem(
                    settings = this.settings,
                    stage = this.stage,
                    lastModified = this.lastModified,
                    isOwned = this.creatorId == user.id,
                    pin = this.pin
                )
            }
        } catch (ex: CompetitionProcessException) {
            error("Can't change competition settings")
        }
    }
}

private fun String?.toCompetitionStage(): CompetitionStage =
    when (val processedStage = this?.trim()?.lowercase()) {
        CompetitionStage.Draft.name.lowercase() ->
            CompetitionStage.Draft
        CompetitionStage.Registration.name.lowercase() ->
            CompetitionStage.Registration
        else -> error("Unknown or unsupported stage $processedStage")
    }


fun CreateCompetitionRequest.extractCompetitionSettings() =
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
        showPreviousRoundResults = shouldShowStudentPreviousRoundResults ?: false,
        endRoundBeforeAllAnswered = shouldEndRoundBeforeAllAnswered ?: false,
        showStudentsResultsTable = shouldShowResultTableInEnd ?: false,
        isAutoRoundEnding = isAutoRoundEnding ?: false,
        showOtherTeamsMembers = showOtherTeamsMembers ?: false,
    )

fun List<Double>.toCompetitionExpensesFormula() =
    CompetitionExpensesFormula(
        xSquareCoefficient = get(0),
        xCoefficient = get(1),
        freeCoefficient = get(2),
    )

fun List<Double>.toCompetitionDemandFormula() =
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
        state = when (stage) {
            is CompetitionStage.WaitingStart -> "InProcess"
            else -> stage.name
        },
        teamLossUpperbound = settings.teamLossLimit.toDouble(),
    )
