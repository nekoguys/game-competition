package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import ru.nekoguys.game.entity.commongame.service.SessionPinGenerator
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionStageChangedMessage
import ru.nekoguys.game.web.dto.RoundEvent
import ru.nekoguys.game.web.dto.StartCompetitionResponse
import ru.nekoguys.game.entity.competition.CompetitionProcessService as CoreCompetitionProcessService

@Service
@Qualifier("webCompetitionProcessService")
class CompetitionProcessService(
    // одноимённый класс, поэтому такое странное название
    private val coreCompetitionProcessService: CoreCompetitionProcessService,
    private val sessionPinGenerator: SessionPinGenerator,
) {
    suspend fun startCompetition(
        pin: String
    ): StartCompetitionResponse? {
        val sessionId = sessionPinGenerator
            .decodeIdFromPin(pin)
            ?: return null

        try {
            coreCompetitionProcessService
                .acceptInternalCommand(
                    sessionId,
                    CompetitionCommand.ChangeStageCommand(
                        from = CompetitionStage.Registration,
                        to = CompetitionStage.InProgress(round = 1),
                    ),
                )
        } catch (ex: CompetitionProcessException) {
            return StartCompetitionResponse.ProcessError(ex.message)
        }

        return StartCompetitionResponse.Success
    }

    fun competitionRoundEventsFlow(
        sessionPin: String,
    ): Flux<RoundEvent> = flow {
        val sessionId = sessionPinGenerator
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        coreCompetitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body } // не смотрим, каким командам отправлено сообщение
            .transform { msg ->
                val notification = when (msg) {
                    is CompetitionStageChangedMessage -> msg.toRoundEvent()
                    else -> return@transform
                }
                emit(notification)
            }
            .map { error("") }
//            .collect(::emit)
        TODO()
    }

    private fun CompetitionStageChangedMessage.toRoundEvent(): RoundEvent =
        RoundEvent.NewRound(
            roundLength =
        )


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
