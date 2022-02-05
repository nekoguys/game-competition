package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionService
import ru.nekoguys.game.web.util.toBadRequestResponse
import ru.nekoguys.game.web.util.toOkResponse
import ru.nekoguys.game.web.util.withMDCContext
import java.security.Principal
import javax.validation.Valid

@Controller
@RequestMapping(
    path = ["/api/competitions"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class CompetitionController(
    private val competitionService: CompetitionService,
) {
    @PostMapping("/create")
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

    private fun CreateCompetitionResponse.toResponseEntity(): ResponseEntity<out CreateCompetitionResponse> =
        when (this) {
            is CreateCompetitionResponse.Created -> toOkResponse()
            is CreateCompetitionResponse.CreatedRegistered -> toOkResponse()
        }

    @GetMapping("/competitions_history/{start}/{amount}")
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

    @PostMapping("/create_team")
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

    private fun CreateTeamResponse.toResponseEntity(): ResponseEntity<out CreateTeamResponse> =
        when (this) {
            is CreateTeamResponse.Success -> toOkResponse()
            is CreateTeamResponse.GameNotFound -> toBadRequestResponse()
        }

    @PostMapping("/join_team")
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

    private fun JoinTeamResponse.toResponseEntity(): ResponseEntity<out JoinTeamResponse> =
        when (this) {
            is JoinTeamResponse.Success -> toOkResponse()
            else -> error("")
        }
}
