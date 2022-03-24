package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinDecoder
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.repository.CompetitionPlayerRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionCreateTeamMessage
import ru.nekoguys.game.entity.competition.rule.CompetitionJoinTeamMessage
import ru.nekoguys.game.entity.competition.service.CompetitionProcessException
import ru.nekoguys.game.entity.competition.service.CompetitionProcessService
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.*

@Service
class CompetitionTeamService(
    private val competitionProcessService: CompetitionProcessService,
    private val sessionPinDecoder: SessionPinDecoder,
    private val userRepository: UserRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
    private val competitionPlayerRepository: CompetitionPlayerRepository,
) {

    suspend fun create(
        sessionPin: String,
        studentEmail: String,
        request: CreateTeamRequest,
    ): TeamApiResponse<CreateTeamResponse> {
        require(request.teamName.length >= 4) {
            return CreateTeamResponse.IncorrectName
        }

        val captain = userRepository
            .findByEmail(studentEmail)
            ?: return TeamApiResponse.UserIsNotRegistered(studentEmail)

        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return TeamApiResponse.SessionNotFound(sessionPin)

        return try {
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = captain,
                command = CompetitionCommand.CreateTeam(
                    teamName = request.teamName,
                    password = request.password.trim(),
                ),
            )
            CreateTeamResponse.Success
        } catch (ex: CompetitionProcessException) {
            TeamApiResponse.ProcessError(ex.message)
        }
    }

    suspend fun join(
        sessionPin: String,
        studentEmail: String,
        request: JoinTeamRequest,
    ): TeamApiResponse<JoinTeamResponse> {
        val captain = userRepository
            .findByEmail(studentEmail)
            ?: return TeamApiResponse.UserIsNotRegistered(studentEmail)

        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return TeamApiResponse.SessionNotFound(sessionPin)

        return try {
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = captain,
                command = CompetitionCommand.JoinTeam(
                    teamName = request.teamName,
                    password = request.password.trim(),
                ),
            )
            JoinTeamResponse.Success(request.teamName)
        } catch (ex: CompetitionProcessException) {
            TeamApiResponse.ProcessError(ex.message)
        }
    }

    suspend fun getTeam(
        sessionPin: String,
        studentEmail: String,
    ): TeamApiResponse<GetTeamResponse>? {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: return TeamApiResponse.SessionNotFound(sessionPin)

        val user = userRepository
            .findByEmail(studentEmail)
            ?: return TeamApiResponse.UserIsNotRegistered(studentEmail)

        val currentPlayerInfo = competitionPlayerRepository
            .load(sessionId, user)
            .let { it as? CompetitionPlayer.Student }
            ?: return TeamApiResponse.UserIsNotRegistered(studentEmail)

        val team = competitionTeamRepository
            .load(currentPlayerInfo.teamId)

        return GetTeamResponse(
            teamName = team.name,
            password = team.password
                .takeIf { currentPlayerInfo is CompetitionPlayer.TeamCaptain }
        )
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

    fun joinUserTeamEventsFlow(
        studentEmail: String,
        sessionPin: String
    ): Flow<TeamMemberUpdateNotification> = flow {
        val sessionId = sessionPinDecoder
            .decodeIdFromPin(sessionPin)
            ?: error("There is no competition session with pin '$sessionPin'")

        val user = userRepository
            .findByEmail(studentEmail)
            ?: error("User with email '$studentEmail' is not registered")

        val teamId = competitionPlayerRepository
            .load(sessionId, user)
            .let { it as? CompetitionPlayer.Student }
            ?.teamId
            ?: error("User with email '$studentEmail' is not associated with session '$sessionId'")

        val team = competitionTeamRepository.load(teamId)

        competitionProcessService.getAllMessagesForSession(sessionId)
            .mapNotNull {
                val msg = it.body
                when {
                    msg is CompetitionCreateTeamMessage && msg.teamName == team.name ->
                        msg.toNewTeamMemberNotification()
                    msg is CompetitionJoinTeamMessage && msg.teamName == team.name ->
                        msg.toNewTeamMemberNotification()
                    else -> null
                }
            }.collect(::emit)
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

private fun CompetitionCreateTeamMessage.toNewTeamMemberNotification() =
    TeamMemberUpdateNotification(
        name = captainEmail,
        isCaptain = true
    )

private fun CompetitionJoinTeamMessage.toNewTeamMemberNotification() =
    TeamMemberUpdateNotification(
        name = newMemberEmail,
        isCaptain = false
    )
