package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.CompetitionProcessService
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionCreateTeamMessage
import ru.nekoguys.game.entity.competition.rule.CompetitionJoinTeamMessage
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.*

@Service
class CompetitionTeamService(
    private val competitionProcessService: CompetitionProcessService,
    private val sessionPinDecoder: SessionPinDecoder,
    private val userRepository: UserRepository,
) {
    suspend fun create(
        sessionPin: String,
        studentEmail: String,
        request: CreateTeamRequest,
    ): CreateTeamResponse {
        require(request.teamName.length >= 4) {
            return CreateTeamResponse.IncorrectName
        }

        val captain = userRepository
            .findByEmail(studentEmail)
            ?: error("No such user: $studentEmail")

        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return CreateTeamResponse.GameNotFound(sessionPin)

        return try {
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = captain,
                command = CompetitionCommand.CreateTeam(
                    teamName = request.teamName,
                    password = request.password,
                ),
            )
            CreateTeamResponse.Success
        } catch (ex: CompetitionProcessException) {
            CreateTeamResponse.ProcessError(ex.message)
        }
    }

    suspend fun join(
        sessionPin: String,
        studentEmail: String,
        request: JoinTeamRequest,
    ): JoinTeamResponse {
        val captain = userRepository
            .findByEmail(studentEmail)
            ?: error("No such user: studentEmail")

        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return JoinTeamResponse.GameNotFound(sessionPin)

        return try {
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = captain,
                command = CompetitionCommand.JoinTeam(
                    teamName = request.teamName,
                ),
            )
            JoinTeamResponse.Success(request.teamName)
        } catch (ex: CompetitionProcessException) {
            JoinTeamResponse.ProcessError(ex.message)
        }
    }

    fun allTeamJoinEventsFlow(
        sessionPin: String,
    ): Flow<TeamUpdateNotification> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        competitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body } // не смотрим, каким командам отправлено сообщение
            .transform { msg ->
                when (msg) {
                    is CompetitionCreateTeamMessage ->
                        emit(msg.toUpdateNotification())
                    is CompetitionJoinTeamMessage ->
                        emit(msg.toUpdateNotification())
                    else -> Unit
                }
            }
            .collect(::emit)
    }
}

private fun CompetitionCreateTeamMessage.toUpdateNotification() =
    TeamUpdateNotification(
        teamName = teamName,
        idInGame = idInGame,
        teamMembers = listOf(captainEmail)
    )

private fun CompetitionJoinTeamMessage.toUpdateNotification() =
    TeamUpdateNotification(
        teamName = teamName,
        idInGame = idInGame,
        teamMembers = membersEmails,
    )
