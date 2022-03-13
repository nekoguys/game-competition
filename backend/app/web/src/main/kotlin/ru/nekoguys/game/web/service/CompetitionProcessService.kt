package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionStageChangedMessage
import ru.nekoguys.game.web.dto.RoundEvent
import ru.nekoguys.game.web.dto.StartCompetitionResponse
import ru.nekoguys.game.entity.competition.CompetitionProcessService as CoreCompetitionProcessService

@Service("webCompetitionProcessService")
class CompetitionProcessService(
    // одноимённый класс уже есть в lib-game, здесь используется import alias
    private val coreCompetitionProcessService: CoreCompetitionProcessService,
    private val sessionPinDecoder: SessionPinDecoder,
) {
    suspend fun startCompetition(
        teacherEmail: String,
        sessionPin: String
    ): StartCompetitionResponse? {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return null

        try {
            coreCompetitionProcessService
                .acceptInternalCommand(
                    sessionId,
                    CompetitionCommand.ChangeStageCommand(
                        from = CompetitionStage.Registration,
                        to = CompetitionStage.InProcess(round = 1),
                    ),
                )
        } catch (ex: CompetitionProcessException) {
            return StartCompetitionResponse.ProcessError(ex.message)
        }

        return StartCompetitionResponse.Success
    }

    fun competitionRoundEventsFlow(
        sessionPin: String,
    ): Flow<RoundEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        coreCompetitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body } // не смотрим, каким командам отправлено сообщение
            .transform { msg ->
                if (msg is CompetitionStageChangedMessage) {
                    emit(msg.toRoundEvent())
                }
            }
            .collect(::emit)
    }

    private fun CompetitionStageChangedMessage.toRoundEvent(): RoundEvent.EndRound =
        RoundEvent.EndRound(
            roundNumber = 0,
            isEndOfGame = false,
            roundLength = roundLength,
        )


    /*
    @RequestMapping(value = "/rounds_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getCompetitionRoundEvents(@PathVariable String pin) {
        log.info("REQUEST: /api/competition_process/{}/rounds_stream", pin);
        return competitionsRepository
            .findByPin(pin)
            .flatMapMany(comp -> gameManagementService.beginEndRoundEvents(comp))
            .map(e -> ServerSentEvent.builder()
                    .data(e)
                    .id("roundStream")
                    .build()
            );
    }
     */


}
