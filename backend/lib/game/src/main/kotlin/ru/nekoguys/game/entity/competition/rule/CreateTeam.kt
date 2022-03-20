package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.service.processError

data class CompetitionCreateTeamMessage(
    val teamName: String,
    val idInGame: Int,
    val captainEmail: String,
) : CompetitionMessage()

@Component
class CompetitionCreateTeamRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
) : CompetitionRule<CompetitionPlayer.Unknown, CompetitionCommand.CreateTeam, CompetitionCreateTeamMessage> {

    override suspend fun process(
        player: CompetitionPlayer.Unknown,
        command: CompetitionCommand.CreateTeam,
    ): List<CompGameMessage<CompetitionCreateTeamMessage>> {
        val session = competitionSessionRepository
            .load(
                player.sessionId,
                CompetitionSession.WithSettings,
                CompetitionSession.WithTeams,
                CompetitionSession.WithStage,
            )

        validateCommand(
            command = command,
            existingTeams = session.teams,
            maxTeamsAmount = session.settings.maxTeamsAmount,
            currentStage = session.stage,
        )

        val newTeam = competitionTeamRepository.create(
            creator = player,
            name = command.teamName,
            password = command.password,
        )

        return createResponse(newTeam, player.user.email)
    }

    private fun validateCommand(
        command: CompetitionCommand.CreateTeam,
        existingTeams: List<CompetitionTeam>,
        maxTeamsAmount: Int,
        currentStage: CompetitionStage,
    ) {
        if (existingTeams.any { it.name == command.teamName }) {
            processError("There is team with same name already")
        }

        if (existingTeams.size >= maxTeamsAmount) {
            processError(
                "There are too much teams in competition, max amount: $maxTeamsAmount"
            )
        }

        if (currentStage != CompetitionStage.Registration) {
            processError("Illegal competition state")
        }
    }

    private fun createResponse(
        newTeam: CompetitionTeam,
        captainEmail: String,
    ) = buildResponse {
        newTeam.id {
            +CompetitionCreateTeamMessage(
                teamName = newTeam.name,
                idInGame = newTeam.numberInGame,
                captainEmail = captainEmail,
            )
        }
    }
}

suspend fun CompetitionCreateTeamRule.createTeam(
    player: CompetitionBasePlayer,
    command: CompetitionCommand.CreateTeam,
): List<CompGameMessage<CompetitionCreateTeamMessage>> =
    when (player) {
        is CompetitionPlayer.TeamCaptain ->
            processError("${player.user.email} is Captain and is in another team already")

        is CompetitionPlayer.TeamMember ->
            processError("Player ${player.user.email} must not be a member of any team")

        is CompetitionPlayer.Unknown -> process(player, command)

        else -> processError("Got unexpected player $player")
    }
