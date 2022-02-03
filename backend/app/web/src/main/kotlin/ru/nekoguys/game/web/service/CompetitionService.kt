package ru.nekoguys.game.web.service

import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinGenerator
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.CreateCompetitionRequest
import ru.nekoguys.game.web.dto.CreateCompetitionResponse
import ru.nekoguys.game.web.dto.GetCompetitionResponse

@Service
class CompetitionService(
    private val userRepository: UserRepository,
    private val sessionRepository: CompetitionSessionRepository,
    private val sessionPinGenerator: SessionPinGenerator,
) {
    suspend fun create(
        userEmail: String,
        request: CreateCompetitionRequest,
    ): CreateCompetitionResponse {
        val user = userRepository.findByEmail(userEmail)
        checkNotNull(user)

        val session = sessionRepository.create(
            userId = user.id,
            settings = request.extractCompetitionSettings(),
            stage = request.state.toCompetitionStage(),
        )

        return if (session.stage == CompetitionStage.Registration) {
            val pin = sessionPinGenerator.convertSessionIdToPin(session.id)
            CreateCompetitionResponse.CreatedRegistered(pin.toString())
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

        return sessionRepository
            .findByCreatorId(user.id.number, limit, offset)
            .map {
                it.toCompetitionHistoryResponseItem(
                    isOwned = true,
                    pin = sessionPinGenerator
                        .convertSessionIdToPin(it.id)
                        .toString(),
                )
            }
    }

    /*
    @GetMapping(value = "/competitions_history/{start}/{amount}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> competitionsHistory(Mono<Principal> principalMono, @PathVariable Integer start, @PathVariable Integer amount) {
        return principalMono
            .map(Principal::getName)
            .flatMapMany(email -> {
            log.info("GET: /api/competitions/competitions_history/{}/{}, email: {}", start, amount, email);
            return pageableCompetitionService.getByEmail(email, start, amount);
        })
        .collectList()
            .map(ResponseEntity::ok);
    }
     */
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
        endRoundBeforeAllAnswered = shouldEndRoundBeforeAllAnswered!!,
        showStudentsResultsTable = shouldShowResultTableInEnd!!,
        isAutoRoundEnding = isAutoRoundEnding!!,
        showOtherTeamsMembers = showOtherTeamsMembers!!,
    )

private fun String?.toCompetitionStage(): CompetitionStage =
    when (val processedStage = this?.trim()?.uppercase()) {
        CompetitionStage.Draft.name -> CompetitionStage.Draft
        CompetitionStage.Registration.name -> CompetitionStage.Registration
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

private fun CompetitionSession.toCompetitionHistoryResponseItem(
    isOwned: Boolean,
    pin: String,
) =
    GetCompetitionResponse(
        demandFormula = listOf(
            properties.settings.demandFormula.freeCoefficient.toString(),
            properties.settings.demandFormula.xCoefficient.toString(),
        ),
        expensesFormula = listOf(
            properties.settings.expensesFormula.xSquareCoefficient.toString(),
            properties.settings.expensesFormula.xCoefficient.toString(),
            properties.settings.expensesFormula.freeCoefficient.toString(),
        ),
        instruction = properties.settings.instruction,
        isAutoRoundEnding = properties.settings.isAutoRoundEnding,
        isOwned = isOwned,
        lastUpdateTime = lastModified,
        maxTeamSize = properties.settings.maxTeamSize,
        maxTeamsAmount = properties.settings.maxTeamsAmount,
        name = properties.settings.name,
        pin = pin,
        roundLength = properties.settings.roundLength,
        roundsCount = properties.settings.roundsCount,
        shouldEndRoundBeforeAllAnswered = properties.settings.endRoundBeforeAllAnswered,
        shouldShowResultTableInEnd = properties.settings.showStudentsResultsTable,
        shouldShowStudentPreviousRoundResults = properties.settings.showPreviousRoundResults,
        showOtherTeamsMembers = properties.settings.showOtherTeamsMembers,
        state = stage.name.lowercase().replaceFirstChar(Char::uppercase),
        teamLossUpperbound = properties.settings.teamLossLimit.toDouble(),
    )
