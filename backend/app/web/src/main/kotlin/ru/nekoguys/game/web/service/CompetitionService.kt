package ru.nekoguys.game.web.service

import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.competition.model.CompetitionDemandFormula
import ru.nekoguys.game.entity.competition.model.CompetitionExpensesFormula
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.CreateCompetitionRequest
import ru.nekoguys.game.web.dto.CreateCompetitionResponse
import ru.nekoguys.game.web.dto.GetCompetitionHistoryResponse
import ru.nekoguys.game.web.util.toOkResponse

@Service
class CompetitionService(
    private val userRepository: UserRepository,
    private val sessionRepository: CompetitionSessionRepository,
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
            val pin = session.id.number.toString()
            CreateCompetitionResponse.CreatedRegistered(pin)
        } else {
            CreateCompetitionResponse.Created
        }
    }

    suspend fun getCompetitionHistory(
        userEmail: String,
        page: Pageable,
    ): ResponseEntity<GetCompetitionHistoryResponse> {
        return GetCompetitionHistoryResponse(emptyList()).toOkResponse()
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
        teamLossUpperbound = teamLossUpperbound!!,
        instruction = instruction!!,
        shouldShowStudentPreviousRoundResults = shouldShowStudentPreviousRoundResults!!,
        shouldEndRoundBeforeAllAnswered = shouldEndRoundBeforeAllAnswered!!,
        shouldShowResultTableInEnd = shouldShowResultTableInEnd!!,
        isAutoRoundEnding = isAutoRoundEnding!!,
        showOtherTeamsMembers = showOtherTeamsMembers!!,
    )

private fun String?.toCompetitionStage(): CompetitionStage =
    when (val processedStage = this?.trim()?.uppercase()) {
        CompetitionStage.Draft.name -> CompetitionStage.Draft
        CompetitionStage.Registration.name -> CompetitionStage.Registration
        else -> error("Unknown or unsupported stage $processedStage")
    }

private fun List<String>.toCompetitionExpensesFormula() =
    CompetitionExpensesFormula(
        xSquareCoefficient = get(0).toDouble(),
        xCoefficient = get(1).toDouble(),
        freeCoefficient = get(2).toDouble(),
    )

private fun List<String>.toCompetitionDemandFormula() =
    CompetitionDemandFormula(
        xCoefficient = get(0).toDouble(),
        freeCoefficient = get(1).toDouble(),
    )

