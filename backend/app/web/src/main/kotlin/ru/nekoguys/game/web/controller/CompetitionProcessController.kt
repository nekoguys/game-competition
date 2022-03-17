package ru.nekoguys.game.web.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nekoguys.game.web.dto.CompetitionInfoForStudentResultsTableResponse
import ru.nekoguys.game.web.dto.RoundEvent
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
}
