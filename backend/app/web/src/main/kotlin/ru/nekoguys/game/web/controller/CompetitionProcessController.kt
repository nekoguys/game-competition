package ru.nekoguys.game.web.controller

import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nekoguys.game.web.dto.RoundEvent
import ru.nekoguys.game.web.dto.StartCompetitionResponse
import ru.nekoguys.game.web.service.CompetitionProcessService
import ru.nekoguys.game.web.util.asServerSentEventStream
import ru.nekoguys.game.web.util.wrapServiceCall
import java.security.Principal

@RestController
@RequestMapping("/api/competition_process/{pin}")
class CompetitionProcessController(
    private val competitionProcessService: CompetitionProcessService,
) {
    @PostMapping(
        "/start_competition",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun startCompetition(
        principal: Principal,
        @PathVariable pin: String,
    ): ResponseEntity<StartCompetitionResponse> =
        wrapServiceCall {
            competitionProcessService
                .startCompetition(
                    teacherEmail = principal.name,
                    sessionPin = pin,
                )
        }

    @RequestMapping(
        "/rounds_stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    fun competitionRoundEventsFlow(
        @PathVariable pin: String,
    ): Flow<ServerSentEvent<RoundEvent>> =
        competitionProcessService
            .competitionRoundEventsFlow(pin)
            .asServerSentEventStream("roundStream")
}
