package ru.nekoguys.game.web.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import ru.nekoguys.game.web.dto.RoundEvent
import ru.nekoguys.game.web.dto.StartCompetitionResponse
import ru.nekoguys.game.web.service.CompetitionProcessService
import ru.nekoguys.game.web.util.toResponseEntity
import ru.nekoguys.game.web.util.withMDCContext

@RestController
@RequestMapping("/api/competition_process/{pin}")
class CompetitionProcessController(
    private val competitionProcessService: CompetitionProcessService,
) {
    @GetMapping(
        "/start_competition",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    ) // TODO: это же должен быть POST, а не GET
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun startCompetition(
        @PathVariable pin: String,
    ): ResponseEntity<StartCompetitionResponse> =
        withMDCContext {
            competitionProcessService
                .startCompetition(pin)
                .toResponseEntity(ifEmpty = HttpStatus.BAD_REQUEST)
        }

    @RequestMapping(
        "/rounds_stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    fun competitionRoundEventsFlow(
        @PathVariable pin: String,
    ): Flux<ServerSentEvent<RoundEvent>> =
        competitionProcessService
            .competitionRoundEventsFlow(pin)


    /*
    @RequestMapping(value = "/rounds_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getCompetitionRoundEvents(@PathVariable String pin) {
        log.info("REQUEST: /api/competition_process/{}/rounds_stream", pin);
        return competitionsRepository.findByPin(pin).flatMapMany(comp -> gameManagementService.beginEndRoundEvents(comp))
                .map(e -> ServerSentEvent.builder().data(e).id("roundStream").build());
    }
     */
}
