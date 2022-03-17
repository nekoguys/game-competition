package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.CompetitionProcessService
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.model.students
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionStageChangedMessage
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
            ?: return null

        try {
            competitionProcessService
                .acceptInternalCommand(
                    sessionId,
                    CompetitionCommand.ChangeStageCommand(
                        from = CompetitionStage.Registration,
                        to = CompetitionStage.InProcess(round = 1),
                    ),
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
        TODO()
    }

    suspend fun changeRoundLength(
        teacherEmail: String,
        sessionPin: String,
        newLength: Int,
    ): ChangeRoundLengthResponse? {
        TODO()
    }

    fun competitionRoundEventsFlow(
        sessionPin: String,
    ): Flow<RoundEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        competitionProcessService
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

    suspend fun getTeacherCompInfo(
        sessionPin: String,
    ): CompetitionInfoForResultsTableResponse? {
        val id = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return null

        val session = competitionSessionRepository
            .load(
                id,
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

    suspend fun getStudentCompInfo(
        studentEmail: String,
        sessionPin: String,
    ): CompetitionInfoForStudentResultsTableResponse? {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return null

        val session = competitionSessionRepository
            .load(
                sessionId,
                CompetitionSession.WithSettings,
                CompetitionSession.WithTeams,
            )

        val settings = session.settings

        val playerInfo = session
            .teams
            .asSequence()
            .flatMap { it.students }
            .find { it.user.email == studentEmail }
            ?: return null

        val team = session
            .teams
            .first { it.id == playerInfo.teamId }

        return CompetitionInfoForStudentResultsTableResponse(
            name = settings.name,
            description = settings.instruction,
            teamName = team.name,
            teamIdInGame = team.numberInGame,
            shouldShowResultTable = settings.showPreviousRoundResults,
            shouldShowResultTableInEnd = settings.showStudentsResultsTable,
            isCaptain = (playerInfo is CompetitionPlayer.TeamCaptain),
            roundsCount = settings.roundsCount,
            strategy = team.strategy.orEmpty(),
        )
    }
}
