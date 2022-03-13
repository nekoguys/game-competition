package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.competitionProcessError
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.entity.competition.repository.load

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
            competitionProcessError("There is team with same name already")
        }

        if (existingTeams.size >= maxTeamsAmount) {
            competitionProcessError(
                "There are too much teams in competition, max amount: $maxTeamsAmount"
            )
        }

        if (currentStage != CompetitionStage.Registration) {
            competitionProcessError("Illegal competition state")
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
