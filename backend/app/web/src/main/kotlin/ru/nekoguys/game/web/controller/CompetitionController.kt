package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionService
import ru.nekoguys.game.web.util.toResponseEntity
import ru.nekoguys.game.web.util.withMDCContext
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
        withMDCContext {
            competitionService.create(
                userEmail = principal.name,
                request = request
            ).toResponseEntity()
        }

    @GetMapping(
        "/competitions_history/{start}/{amount}",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun competitionsHistory(
        principal: Principal,
        @PathVariable start: Int,
        @PathVariable amount: Int,
    ): ResponseEntity<List<GetCompetitionResponse>> =
        withMDCContext {
            competitionService.getCompetitionHistory(
                userEmail = principal.name,
                limit = amount,
                offset = start,
            ).let { ResponseEntity.ok(it) }
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
        competitionService
            .ifSessionCanBeJoined(sessionPin = request.pin)
            .let(::CheckGamePinResponse)
            .toResponseEntity()

    @GetMapping(
        "/get_clone_info/{sessionPin}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun getCompetitionInfo(
        @PathVariable sessionPin: String,
    ) {
        withMDCContext {
            competitionService
                .getCompetitionCloneInfo(sessionPin)
                .toResponseEntity()
        }
    }
}
