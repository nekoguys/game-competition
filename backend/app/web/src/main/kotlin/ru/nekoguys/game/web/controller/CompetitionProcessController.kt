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
    @RequestMapping(
        "/rounds_stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    fun competitionRoundEventsFlow(
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<RoundEvent>> =
        competitionProcessService
            .competitionRoundEventsFlow(sessionPin)
            .asServerSentEventStream("roundStream")

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

    @RequestMapping(
        "/student_all_in_one",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun studentAllInOneFlow(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<out Any>> =
        merge(
            competitionProcessService
                .competitionRoundEventsFlow(sessionPin)
                .asServerSentEventStream("roundStream"),
            competitionProcessService
                .myAnswersEventsFlow(sessionPin, principal.name)
                .asServerSentEventStream("answerStream")
        )
    /*
    @RequestMapping(value = "/student_all_in_one", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getAllStudent(Mono<Principal> principalMono, @PathVariable String pin) {
        return Flux.merge(
                getMyTeamAnswersEvents(principalMono, pin),
                getMyTeamResultsEvents(principalMono, pin),
                getCompetitionRoundEvents(pin),
                getCompetitionMessages(pin),
                getPricesEvents(pin)
        );
    }
     */

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

    @RequestMapping(
        "/my_results_stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    @PreAuthorize("hasRole('STUDENT')")
    fun myAnswersEventStream(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<Any>> =
        competitionProcessService
            .myAnswersEventsFlow(
                sessionPin = sessionPin,
                studentEmail = principal.name,
            )
            .asServerSentEventStream("answerStream")

    /*
    @RequestMapping(value = "/my_answers_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getMyTeamAnswersEvents(Mono<Principal> principalMono, @PathVariable String pin) {
        return Mono.zip(principalMono, competitionsRepository.findByPin(pin))
                .flatMapMany(tuple -> {
                    var comp = tuple.getT2();
                    var email = tuple.getT1().getName();
                    var team = this.teamFinder.findTeamForStudent(comp, email);
                    log.info("REQUEST: /api/competition_process/{}/my_answers_stream, email: {}", pin, email);

                    if (team.isEmpty()) {
                        return Flux.empty();
                    }

                    return gameManagementService.teamsAnswersEvents(comp).filter(roundTeamAnswerDto -> {
                        return roundTeamAnswerDto.getTeamIdInGame() == team.get().getIdInGame() || roundTeamAnswerDto.getTeamIdInGame() == -1;
                    });
                }).map(e -> ServerSentEvent.builder().data(e).id("answerStream").build());
    }
     */
}
