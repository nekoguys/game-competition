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
import ru.nekoguys.game.web.service.CompetitionTeacherProcessService
import ru.nekoguys.game.web.util.asServerSentEventStream
import ru.nekoguys.game.web.util.wrapServiceCall
import java.security.Principal

@RestController
@RequestMapping("/api/competition_process/{sessionPin}")
class CompetitionTeacherProcessController(
    private val competitionProcessService: CompetitionProcessService,
    private val competitionTeacherProcessService: CompetitionTeacherProcessService,
) {
    @PostMapping(
        "/start_competition",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun startCompetition(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<ProcessApiResponse<StartCompetitionResponse>> =
        wrapServiceCall {
            competitionTeacherProcessService
                .startCompetition(
                    teacherEmail = principal.name,
                    sessionPin = sessionPin,
                )
        }

    @PostMapping(
        "/start_round",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun startRound(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<ProcessApiResponse<StartRoundResponse>> =
        wrapServiceCall {
            competitionTeacherProcessService
                .startRound(
                    teacherEmail = principal.name,
                    sessionPin = sessionPin,
                )
        }

    @PostMapping(
        "/end_round",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun endRound(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<ProcessApiResponse<EndRoundResponse>> =
        wrapServiceCall {
            competitionTeacherProcessService
                .endRound(
                    teacherEmail = principal.name,
                    sessionPin = sessionPin,
                )
        }

    @PostMapping(
        "/change_round_length",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun changeRoundLength(
        principal: Principal,
        @PathVariable sessionPin: String,
        @RequestBody request: ChangeRoundLengthRequest,
    ): ResponseEntity<ChangeRoundLengthResponse> =
        wrapServiceCall {
            competitionTeacherProcessService
                .changeRoundLength(
                    teacherEmail = principal.name,
                    sessionPin = sessionPin,
                    newLength = request.newRoundLength,
                )
        }

    @GetMapping(
        value = ["/comp_info", "/teacher_comp_info"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun getTeacherCompInfo(
        @PathVariable sessionPin: String,
    ): ResponseEntity<CompetitionInfoForResultsTableResponse> =
        wrapServiceCall {
            competitionTeacherProcessService
                .getTeacherCompInfo(
                    sessionPin = sessionPin,
                )
        }

    @RequestMapping(
        "/teacher_all_in_one",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun teacherAllInOneFlow(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<out Any>> =
        merge(
            competitionProcessService
                .competitionRoundEventsFlow(sessionPin)
                .asServerSentEventStream("roundStream"),
            competitionTeacherProcessService
                .allTeamsAnswersFlow(sessionPin)
                .asServerSentEventStream("answerStream"),
            competitionProcessService
                .priceEventsFlow(sessionPin)
                .asServerSentEventStream("pricesStream"),
            competitionTeacherProcessService
                .allTeamsResultsFlow(sessionPin)
                .asServerSentEventStream("resultStream"),
            competitionProcessService
                .bansEventsFlow(sessionPin)
                .asServerSentEventStream("banStream")
        )

    /*
    return Flux.merge(
                getTeamsAnswers(pin),
                getResultsEvents(pin),
                getCompetitionRoundEvents(pin),
                getCompetitionMessages(pin),
                getPricesEvents(pin),
                getBanEvents(pin)
        );
     */

    // TODO: send_message
    // TODO: restart_game

    @RequestMapping(
        "/answers_stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    @PreAuthorize("hasRole('TEACHER')")
    fun allTeamsAnswersEvents(
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<SubmittedAnswerEvent>> =
        competitionTeacherProcessService
            .allTeamsAnswersFlow(sessionPin)
            .asServerSentEventStream("answerStream")

    @RequestMapping(
        "/results_stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    @PreAuthorize("hasRole('TEACHER')")
    fun allTeamsResultsEvents(
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<RoundTeamResultEvent>> =
        competitionTeacherProcessService
            .allTeamsResultsFlow(sessionPin)
            .asServerSentEventStream("resultStream")
}
