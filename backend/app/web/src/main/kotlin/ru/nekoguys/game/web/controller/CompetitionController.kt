package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionService
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
                    offset = page * pageSize, // TODO: modify front page => page * pageSize
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
        @PathVariable sessionPin: String,
    ): ResponseEntity<CompetitionCloneInfoResponse> =
        wrapServiceCall {
            competitionService
                .getCompetitionCloneInfo(sessionPin)
        }
}
