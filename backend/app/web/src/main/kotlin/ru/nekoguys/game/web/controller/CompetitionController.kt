package ru.nekoguys.game.web.controller

import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.CreateCompetitionRequest
import ru.nekoguys.game.web.dto.CreateCompetitionResponse
import ru.nekoguys.game.web.dto.GetCompetitionHistoryResponse
import ru.nekoguys.game.web.service.CompetitionService
import ru.nekoguys.game.web.util.withMDCContext
import java.security.Principal
import javax.validation.Valid

@Controller
@RequestMapping(path = ["/api/competitions"], produces = [MediaType.APPLICATION_JSON_VALUE])
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
            is CreateCompetitionResponse.Created -> ResponseEntity.ok(this)
            is CreateCompetitionResponse.CreatedRegistered -> ResponseEntity.ok(this)
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
    ): ResponseEntity<GetCompetitionHistoryResponse> =
        withMDCContext {
            competitionService.getCompetitionHistory(
                userEmail = principal.name,
                page = PageRequest.of(start / amount, amount),
            )
        }
}
