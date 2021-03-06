package ru.nekoguys.game.web.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.CompetitionProcessService
import ru.nekoguys.game.web.service.CompetitionTeacherProcessService
import ru.nekoguys.game.web.util.asServerSentEventStream
import ru.nekoguys.game.web.util.wrapServiceCall
import java.security.Principal

@RestController
@RequestMapping("/api/competition_process/{sessionPin}")
class CompetitionTeacherProcessController(
    private val competitionProcessService: CompetitionProcessService,
    private val competitionTeacherProcessService: CompetitionTeacherProcessService,
) {
    @GetMapping(
        value = ["/comp_info", "/teacher_comp_info"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun getTeacherCompInfo(
        @PathVariable sessionPin: String,
    ): ResponseEntity<CompetitionInfoForResultsTableResponse> =
        wrapServiceCall {
            competitionTeacherProcessService
                .getTeacherCompInfo(
                    sessionPin = sessionPin,
                )
        }

    @PostMapping(
        "/start_competition",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun startCompetition(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<ProcessApiResponse<StartCompetitionResponse>> =
        wrapServiceCall {
            competitionTeacherProcessService
                .startCompetition(
                    teacherEmail = principal.name,
                    sessionPin = sessionPin,
                )
        }

    @PostMapping(
        "/start_round",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun startRound(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<ProcessApiResponse<StartRoundResponse>> =
        wrapServiceCall {
            competitionTeacherProcessService
                .startRound(
                    teacherEmail = principal.name,
                    sessionPin = sessionPin,
                )
        }

    @PostMapping(
        "/end_round",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun endRound(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): ResponseEntity<ProcessApiResponse<EndRoundResponse>> =
        wrapServiceCall {
            competitionTeacherProcessService
                .endRound(
                    teacherEmail = principal.name,
                    sessionPin = sessionPin,
                )
        }

    @PostMapping(
        "/change_round_length",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun changeRoundLength(
        principal: Principal,
        @PathVariable sessionPin: String,
        @RequestBody request: ChangeRoundLengthRequest,
    ): ResponseEntity<ChangeRoundLengthResponse> =
        wrapServiceCall {
            competitionTeacherProcessService
                .changeRoundLength(
                    teacherEmail = principal.name,
                    sessionPin = sessionPin,
                    newLength = request.newRoundLength,
                )
        }

    @PostMapping(
        "/send_message",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasRole('TEACHER')")
    suspend fun sendAnnouncement(
        principal: Principal,
        @PathVariable sessionPin: String,
        @RequestBody request: SendAnnouncementRequest,
    ): ResponseEntity<ProcessApiResponse<SendAnnouncementResponse>> =
        wrapServiceCall {
            competitionTeacherProcessService.sendAnnouncement(
                teacherEmail = principal.name,
                sessionPin = sessionPin,
                announcement = request.message,
            )
        }

    @RequestMapping(
        "/teacher_all_in_one",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    @PreAuthorize("hasRole('TEACHER')")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun teacherAllInOneFlow(
        principal: Principal,
        @PathVariable sessionPin: String,
    ): Flow<ServerSentEvent<out Any>> =
        merge(
            competitionProcessService
                .competitionRoundEventsFlow(sessionPin)
                .asServerSentEventStream("roundStream"),
            competitionTeacherProcessService
                .allTeamsAnswersFlow(sessionPin)
                .asServerSentEventStream("answerStream"),
            competitionProcessService
                .priceEventsFlow(sessionPin)
                .asServerSentEventStream("pricesStream"),
            competitionTeacherProcessService
                .allTeamsResultsFlow(sessionPin)
                .asServerSentEventStream("resultStream"),
            competitionProcessService
                .bansEventsFlow(sessionPin)
                .asServerSentEventStream("banStream"),
            competitionProcessService
                .announcementsEventsFlow(sessionPin)
                .asServerSentEventStream("messagesStream"),
        )
}
