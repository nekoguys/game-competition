package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionService
import ru.nekoguys.game.web.service.extractCompetitionSettings
import ru.nekoguys.game.web.util.withMDCContext
import ru.nekoguys.game.web.util.wrapServiceCall
import java.security.Principal
import javax.validation.Valid

@RestController
@RequestMapping("/api/competitions")
class CompetitionController(
    private val competitionService: CompetitionService,
) {
    @PostMapping(
        "/create",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun create(
        principal: Principal,
        @RequestBody @Valid request: CreateCompetitionRequest,
    ): ResponseEntity<out CreateCompetitionResponse> =
        wrapServiceCall {
            competitionService.create(
                userEmail = principal.name,
                request = request
            )
        }

    @GetMapping(
        "/competitions_history/{page}/{pageSize}",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun competitionsHistory(
        principal: Principal,
        @PathVariable page: Int,
        @PathVariable pageSize: Int,
    ): ResponseEntity<List<GetCompetitionResponse>> =
        withMDCContext {
            competitionService
                .getCompetitionHistory(
                    userEmail = principal.name,
                    limit = pageSize,
                    offset = page * pageSize,
                )
                .let { ResponseEntity.ok(it) }
        }

    @PostMapping(
        "/check_pin",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun checkIfSessionCanBeJoined(
        @RequestBody request: CheckGamePinRequest,
    ): ResponseEntity<CheckGamePinResponse> =
        wrapServiceCall {
            competitionService
                .ifSessionCanBeJoined(sessionPin = request.pin)
                .let(::CheckGamePinResponse)
        }

    @GetMapping(
        "/get_clone_info/{sessionPin}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun getCompetitionInfo(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<GetCompetitionResponse> =
        wrapServiceCall {
            competitionService
                .getCompetitionCloneInfo(principal.name, sessionPin)
        }


    @PostMapping("/update_competition/{sessionPin}")
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun changeCompetitionInfo(
        principal: Principal,
        @PathVariable sessionPin: String,
        @RequestBody @Valid request: CreateCompetitionRequest,
    ): ResponseEntity<GetCompetitionResponse> =
        wrapServiceCall {
            competitionService.changeCompetitionSettings(
                userEmail = principal.name,
                sessionPin = sessionPin,
                competitionSettings = request.extractCompetitionSettings(),
                state = request.state
            )
        }

    @GetMapping(value = ["/competition_results/{sessionPin}"])
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun getCompetitionResults(
        @PathVariable sessionPin: String,
    ): ResponseEntity<CompetitionResultsResponse> =
        wrapServiceCall {
            competitionService.getCompetitionResults(
                sessionPin = sessionPin
            )
        }
    /*
    @GetMapping(value = "/competition_results/{pin}")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> competitionResults(@PathVariable String pin) {
        log.info("GET: /api/competitions/competition_results/{}", pin);
        return this.competitionsRepository.findByPin(pin).map(el -> {
            return (ResponseEntity)ResponseEntity.ok(resultsFormatter.getCompetitionResults(el));
        }).switchIfEmpty(Mono.defer(() -> {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("Competition with pin: " + pin + " not found")));
        })).onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of(ex.getMessage()))));
    }
     */
}
