package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.CompetitionProcessService
import ru.nekoguys.game.entity.competition.changeStage
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.findAll
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.rule.CompetitionAnswerSubmittedMessage
import ru.nekoguys.game.web.dto.*

@Service
class CompetitionTeacherProcessService(
    private val competitionProcessService: CompetitionProcessService,
    private val sessionPinDecoder: SessionPinDecoder,
    private val competitionSessionRepository: CompetitionSessionRepository,
) {
    suspend fun startCompetition(
        teacherEmail: String,
        sessionPin: String
    ): ProcessApiResponse<StartCompetitionResponse>? {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return ProcessApiResponse.SessionNotFound(sessionPin)

        try {
            competitionProcessService
                .changeStage(
                    sessionId,
                    from = CompetitionStage.Registration,
                    to = CompetitionStage.WaitingStart(round = 1),
                )
        } catch (ex: CompetitionProcessException) {
            return ProcessApiResponse.ProcessError(ex.message)
        }

        return StartCompetitionResponse
    }

    suspend fun startRound(
        teacherEmail: String,
        sessionPin: String,
    ): ProcessApiResponse<StartRoundResponse>? {
        val sessionId = sessionPinDecoder
            .decodeIdFromPinUnsafe(sessionPin)
            ?: return ProcessApiResponse.SessionNotFound(sessionPin)

        val session = competitionSessionRepository
            .findAll(listOf(sessionId), CompetitionSession.WithStage)
            .firstOrNull() ?: return ProcessApiResponse.SessionNotFound(sessionPin)

        val stage = session.stage
        if (stage is CompetitionStage.WaitingStart) {
            try {
                competitionProcessService
                    .changeStage(
                        sessionId = session.id,
                        from = stage,
                        to = CompetitionStage.InProcess(stage.round)
                    )
            } catch (ex: CompetitionProcessException) {
                return ProcessApiResponse.ProcessError(ex.message)
            }
        }

        return StartRoundResponse
    }

    suspend fun changeRoundLength(
        teacherEmail: String,
        sessionPin: String,
        newLength: Int,
    ): ChangeRoundLengthResponse? {
        TODO()
    }

    suspend fun getTeacherCompInfo(
        sessionPin: String,
    ): CompetitionInfoForResultsTableResponse? {

        val session = competitionSessionRepository
            .load(
                id = sessionPinDecoder
                    .decodeIdFromPin(sessionPin)
                    ?: return null,
                CompetitionSession.WithSettings,
                CompetitionSession.WithTeamIds,
            )

        return CompetitionInfoForResultsTableResponse(
            connectedTeamsCount = session.teamIds.size,
            roundsCount = session.settings.roundsCount,
            name = session.settings.name,
            isAutoRoundEnding = session.settings.isAutoRoundEnding,
        )
    }

    fun allTeamAnswersFlow(
        sessionPin: String,
    ): Flow<SubmittedAnswerEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        val teamNumberById = competitionSessionRepository
            .load(sessionId, CompetitionSession.WithTeams)
            .teams
            .associateBy(CompetitionTeam::id, CompetitionTeam::numberInGame)

        competitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body }
            .filterIsInstance<CompetitionAnswerSubmittedMessage>()
            .mapNotNull { msg ->
                SubmittedAnswerEvent(
                    teamIdInGame = teamNumberById.getValue(msg.teamId),
                    roundNumber = msg.roundNumber,
                    teamAnswer = msg.answer,
                )
            }
            .collect(::emit)
    }
}
