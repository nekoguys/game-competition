package ru.nekoguys.game.web.controller

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionService
import ru.nekoguys.game.web.util.toOkResponse
import ru.nekoguys.game.web.util.toResponseEntity
import ru.nekoguys.game.web.util.withMDCContext
import ru.nekoguys.game.web.util.withRequestIdInContext
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
            ).toOkResponse()
        }

    @PostMapping(
        "/create_team",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun createTeam(
        principal: Principal,
        @RequestBody request: CreateTeamRequest,
    ): ResponseEntity<out CreateTeamResponse> =
        withMDCContext {
            competitionService.createTeam(
                studentEmail = principal.name,
                request = request,
            ).toResponseEntity()
        }

    @PostMapping(
        "/join_team",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('STUDENT')")
    suspend fun joinTeam(
        principal: Principal,
        @RequestBody request: JoinTeamRequest,
    ): ResponseEntity<out JoinTeamResponse> =
        withMDCContext {
            competitionService.joinTeam(
                studentEmail = principal.name,
                request = request,
            ).toResponseEntity()
        }

    @RequestMapping(
        "/team_join_events/{pin}",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun teamJoinEvents(
        principal: Principal,
        @PathVariable pin: String,
    ): Flow<ServerSentEvent<TeamUpdateNotification>> =
        competitionService
            .teamJoinMessageFlow(
                userEmail = principal.name,
                sessionPin = pin,
            )
            .map { message ->
                ServerSentEvent.builder(message).id("teamStream").build()
            }
            .withRequestIdInContext()

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
        "/get_clone_info/{pin}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun getCompetitionInfo(@PathVariable pin: String) {
        withMDCContext {
            competitionService
                .getCompetitionCloneInfo(pin)
                .toResponseEntity()
        }
    }
}
