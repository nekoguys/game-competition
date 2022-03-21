package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.commongame.service.toPin
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionPlayerRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.rule.*
import ru.nekoguys.game.entity.competition.service.CompetitionProcessException
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import ru.nekoguys.game.entity.competition.service.CompetitionProcessService as CoreCompetitionProcessService

@Service("webCompetitionProcessService")
class CompetitionProcessService(
    // одноимённый класс уже есть в lib-game, здесь используется import alias
    private val coreCompetitionProcessService: CoreCompetitionProcessService,
    private val userRepository: UserRepository,
    private val sessionPinDecoder: SessionPinDecoder,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val competitionPlayerRepository: CompetitionPlayerRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
) {

    suspend fun submitAnswer(
        studentEmail: String,
        sessionPin: String,
        roundNumber: Int,
        answer: Int,
    ): ProcessApiResponse<SubmitAnswerResponse> =
        doCommand(
            sessionPin = sessionPin,
            studentEmail = studentEmail,
            command = CompetitionCommand.SubmitAnswer(
                currentRound = roundNumber,
                answer = answer,
            ),
            onSuccess = SubmitAnswerResponse,
        )

    fun competitionRoundEventsFlow(
        sessionPin: String,
    ): Flow<RoundEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        coreCompetitionProcessService
            .getAllMessagesForSession(sessionId)
            .mapNotNull { msg ->
                (msg.body as? CompetitionStageChangedMessage)?.toRoundEvent()
            }
            .collect(::emit)
    }

    private fun CompetitionStageChangedMessage.toRoundEvent(): RoundEvent? =
        when {
            from is CompetitionStage.InProcess ->
                RoundEvent.EndRound(
                    roundNumber = (from as CompetitionStage.InProcess).round,
                    isEndOfGame = false,
                    roundLength = roundLength,
                )

            to is CompetitionStage.InProcess ->
                RoundEvent.NewRound(
                    roundLength = roundLength,
                    beginTime = timeStamp.toEpochSecond(ZoneOffset.UTC),
                    roundNumber = (to as CompetitionStage.InProcess).round,
                )

            else -> null
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

    fun myAnswersEventsFlow(
        sessionPin: String,
        studentEmail: String,
    ): Flow<SubmittedAnswerEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        val studentTeam = getStudentTeam(sessionId, studentEmail)

        coreCompetitionProcessService
            .getAllMessagesForTeam(sessionId, studentTeam.id)
            .filterIsInstance<CompetitionAnswerSubmittedMessage>()
            .map { msg ->
                SubmittedAnswerEvent(
                    teamIdInGame = studentTeam.numberInGame,
                    roundNumber = msg.roundNumber,
                    teamAnswer = msg.answer,
                )
            }
            .collect(::emit)
    }

    fun myResultsEventsFlow(
        sessionPin: String,
        studentEmail: String,
    ): Flow<RoundTeamResultEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        val studentTeam = getStudentTeam(sessionId, studentEmail)

        coreCompetitionProcessService
            .getAllMessagesForTeam(sessionId, studentTeam.id)
            .filterIsInstance<CompetitionRoundResultsMessage>()
            .map { msg ->
                RoundTeamResultEvent(
                    teamIdInGame = studentTeam.numberInGame,
                    roundNumber = msg.roundNumber,
                    income = msg.income,
                )
            }
            .collect(::emit)
    }

    private suspend fun getStudentTeam(
        sessionId: CommonSession.Id,
        studentEmail: String,
    ): CompetitionTeam {
        val student = userRepository
            .findByEmail(studentEmail)
            ?: error("No student with email $studentEmail")

        val studentPlayerInfo = competitionPlayerRepository
            .load(sessionId = sessionId, user = student)
        if (studentPlayerInfo !is CompetitionPlayer.Student) {
            error("Player $studentEmail is not registered in session ${sessionId.toPin()}")
        }

        return competitionTeamRepository
            .load(studentPlayerInfo.teamId)
    }

    fun priceEventsFlow(
        sessionPin: String,
    ): Flow<PriceChangeEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        coreCompetitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body }
            .filterIsInstance<CompetitionPriceChangeMessage>()
            .map { msg ->
                PriceChangeEvent(
                    roundNumber = msg.roundNumber,
                    price = msg.price,
                )
            }
            .collect(::emit)
    }

    fun announcementsEventsFlow(
        sessionPin: String,
    ): Flow<AnnouncementEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")
        coreCompetitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body }
            .filterIsInstance<CompetitionAnnouncementMessage>()
            .map { msg ->
                AnnouncementEvent(
                    message = msg.announcement,
                    sendTime = LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond()
                )
            }
            .collect(::emit)
    }

    fun bansEventsFlow(
        sessionPin: String,
    ): Flow<TeamBanEvent> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        coreCompetitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body }
            .filterIsInstance<CompetitionTeamBannedMessage>()
            .map { msg ->
                TeamBanEvent(
                    teamIdInGame = msg.teamNumber,
                    teamName = msg.teamName,
                    roundNumber = msg.roundNumber,
                    reason = msg.reason,
                )
            }
            .collect(::emit)
    }

    private suspend fun <T : ProcessApiResponse<T>> doCommand(
        sessionPin: String,
        studentEmail: String,
        command: CompetitionCommand,
        onSuccess: T,
    ): ProcessApiResponse<T> {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return ProcessApiResponse.SessionNotFound(sessionPin)

        val user = userRepository
            .findByEmail(studentEmail)
            ?: error("User with email $studentEmail doesn't exist")

        return try {
            coreCompetitionProcessService
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
