package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.rule.CompetitionAnswerSubmittedMessage
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionRoundResultsMessage
import ru.nekoguys.game.entity.competition.service.CompetitionProcessException
import ru.nekoguys.game.entity.competition.service.CompetitionProcessService
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.*

@Service
class CompetitionTeacherProcessService(
    private val competitionProcessService: CompetitionProcessService,
    private val sessionPinDecoder: SessionPinDecoder,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val userRepository: UserRepository,
) {
    suspend fun startCompetition(
        teacherEmail: String,
        sessionPin: String,
    ): ProcessApiResponse<StartCompetitionResponse> =
        doCommand(
            sessionPin = sessionPin,
            teacherEmail = teacherEmail,
            command = CompetitionCommand.Start,
            onSuccess = StartCompetitionResponse,
        )

    suspend fun startRound(
        teacherEmail: String,
        sessionPin: String,
    ): ProcessApiResponse<StartRoundResponse>? =
        doCommand(
            sessionPin = sessionPin,
            teacherEmail = teacherEmail,
            command = CompetitionCommand.StartRound,
            onSuccess = StartRoundResponse,
        )

    suspend fun endRound(
        teacherEmail: String,
        sessionPin: String,
    ): ProcessApiResponse<EndRoundResponse> =
        doCommand(
            sessionPin = sessionPin,
            teacherEmail = teacherEmail,
            command = CompetitionCommand.EndCurrentRound,
            onSuccess = EndRoundResponse,
        )

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

    suspend fun sendAnnouncement(
        teacherEmail: String,
        sessionPin: String,
        announcement: String,
    ): ProcessApiResponse<SendAnnouncementResponse> {
        val user = userRepository.findByEmail(teacherEmail)
        requireNotNull(user)
        val sessionId = sessionPinDecoder.decodeIdFromPin(sessionPin)
            ?: error("No competition with pin $sessionPin")
        val session = competitionSessionRepository.load(
            id = sessionId,
            CompetitionSession.WithCommonFields
        )
        if (session.creatorId != user.id) {
            return ProcessApiResponse.ProcessError(
                "${user.email} is not game creator"
            )
        }

        competitionProcessService.acceptInternalCommand(
            sessionId = sessionId,
            command = CompetitionCommand.SendAnnouncement(
                announcement = announcement,
            )
        )
        return SendAnnouncementResponse("Announcement sent")
    }

    fun allTeamsAnswersFlow(
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
            .collect {
                val msg = it.body
                val teamId = it.players.single()
                if (msg is CompetitionAnswerSubmittedMessage) {
                    val event = SubmittedAnswerEvent(
                        teamIdInGame = teamNumberById.getValue(teamId),
                        roundNumber = msg.roundNumber,
                        teamAnswer = msg.answer,
                    )
                    emit(event)
                }
            }
    }

    fun allTeamsResultsFlow(
        sessionPin: String,
    ): Flow<RoundTeamResultEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        val teamNumberById = competitionSessionRepository
            .load(sessionId, CompetitionSession.WithTeams)
            .teams
            .associateBy(CompetitionTeam::id, CompetitionTeam::numberInGame)

        competitionProcessService
            .getAllMessagesForSession(sessionId)
            .collect {
                val msg = it.body
                val teamId = it.players.single()
                if (msg is CompetitionRoundResultsMessage) {
                    val event = RoundTeamResultEvent(
                        teamIdInGame = teamNumberById.getValue(teamId),
                        roundNumber = msg.roundNumber,
                        income = msg.income,
                    )
                    emit(event)
                }
            }
    }

    private suspend fun <T : ProcessApiResponse<T>> doCommand(
        sessionPin: String,
        teacherEmail: String,
        command: CompetitionCommand,
        onSuccess: T,
    ): ProcessApiResponse<T> {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return ProcessApiResponse.SessionNotFound(sessionPin)

        val user = userRepository
            .findByEmail(teacherEmail)
            ?: error("User with email $teacherEmail doesn't exist")

        return try {
            competitionProcessService
                .acceptCommand(
                    sessionId = sessionId,
                    user = user,
                    command = command
                )
            onSuccess
        } catch (ex: CompetitionProcessException) {
            ProcessApiResponse.ProcessError(ex.message)
        }
    }
}
