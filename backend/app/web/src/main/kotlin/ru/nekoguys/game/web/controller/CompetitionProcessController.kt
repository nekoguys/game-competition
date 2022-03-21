package ru.nekoguys.game.web.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionProcessService
import ru.nekoguys.game.web.util.asServerSentEventStream
import ru.nekoguys.game.web.util.wrapServiceCall
import java.security.Principal

@RestController
@RequestMapping("/api/competition_process/{sessionPin}")
class CompetitionProcessController(
    private val competitionProcessService: CompetitionProcessService,
) {

    @GetMapping(
        "/student_comp_info",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun getStudentCompInfo(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<CompetitionInfoForStudentResultsTableResponse> =
        wrapServiceCall {
            competitionProcessService
                .getStudentCompInfo(
                    studentEmail = principal.name,
                    sessionPin = sessionPin,
                )
        }

    @PostMapping(
        "/submit_answer",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun submitAnswer(
        principal: Principal,
        @PathVariable sessionPin: String,
        @RequestBody request: SubmitAnswerRequest,
    ): ResponseEntity<ProcessApiResponse<SubmitAnswerResponse>> =
        wrapServiceCall {
            competitionProcessService
                .submitAnswer(
                    studentEmail = principal.name,
                    sessionPin = sessionPin,
                    roundNumber = request.roundNumber,
                    answer = request.answer,
                )
        }

    @PostMapping(
        "/submit_strategy",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun submitStrategy(
        principal: Principal,
        @PathVariable sessionPin: String,
        @RequestBody request: SubmitStrategyRequest,
    ): ResponseEntity<ProcessApiResponse<SubmitStrategyResponse>> =
        wrapServiceCall {
            competitionProcessService
                .submitStrategy(
                    studentEmail = principal.name,
                    sessionPin = sessionPin,
                    strategy = request.strategy,
                )
        }

    @RequestMapping(
        "/student_all_in_one",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun allStudentEventsStream(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<out Any>> =
        merge(
            competitionProcessService
                .competitionRoundEventsFlow(sessionPin)
                .asServerSentEventStream("roundStream"),
            competitionProcessService
                .myAnswersEventsFlow(sessionPin, principal.name)
                .asServerSentEventStream("answerStream"),
            competitionProcessService
                .myResultsEventsFlow(sessionPin, principal.name)
                .asServerSentEventStream("answerStream"),
            competitionProcessService
                .priceEventsFlow(sessionPin)
                .asServerSentEventStream("pricesStream"),
            competitionProcessService
                .announcementsEventsFlow(sessionPin)
                .asServerSentEventStream("messagesStream"),
        )
}
