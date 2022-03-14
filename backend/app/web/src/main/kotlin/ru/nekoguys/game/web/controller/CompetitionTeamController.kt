package ru.nekoguys.game.web.controller

import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionTeamService
import ru.nekoguys.game.web.util.asServerSentEventStream
import ru.nekoguys.game.web.util.wrapServiceCall
import java.security.Principal

@RestController
@RequestMapping("/api/competitions/{sessionPin}/teams")
@PreAuthorize("hasRole('STUDENT')")
class CompetitionTeamController(
    private val competitionTeamService: CompetitionTeamService,
) {
    @PostMapping(
        "/create",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    suspend fun create(
        principal: Principal,
        @PathVariable sessionPin: String,
        @RequestBody request: CreateTeamRequest,
    ): ResponseEntity<TeamApiResponse<CreateTeamResponse>> =
        wrapServiceCall {
            competitionTeamService.create(
                sessionPin = sessionPin,
                studentEmail = principal.name,
                request = request,
            )
        }

    @PostMapping(
        "/join",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    suspend fun join(
        principal: Principal,
        @PathVariable sessionPin: String,
        @RequestBody request: JoinTeamRequest,
    ): ResponseEntity<TeamApiResponse<JoinTeamResponse>> =
        wrapServiceCall {
            competitionTeamService.join(
                sessionPin = sessionPin,
                studentEmail = principal.name,
                request = request,
            )
        }

    @GetMapping(
        "/current",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    suspend fun getCurrentTeamInfo(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<TeamApiResponse<GetTeamResponse>> =
        wrapServiceCall {
            competitionTeamService.getTeam(
                sessionPin = sessionPin,
                studentEmail = principal.name,
            )
        }

    @RequestMapping(
        "/all_join_events",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun allJoinEvents(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<TeamUpdateNotification>> =
        competitionTeamService
            .allTeamJoinEventsFlow(sessionPin = sessionPin)
            .asServerSentEventStream("teamStream")

    @GetMapping(
        "/my_team_new_members",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    @PreAuthorize("hasRole('STUDENT')")
    fun myTeamNewMembers(
        principal: Principal,
        @PathVariable sessionPin: String
    ): Flow<ServerSentEvent<TeamMemberUpdateNotification>> =
        competitionTeamService
            .joinUserTeamEventsFlow(studentEmail = principal.name, sessionPin = sessionPin)
            .asServerSentEventStream("myTeamStream")
}
