package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository

data class CompetitionCreateTeamMessage(
    val teamName: String,
    val idInGame: Int,
    val captainEmail: String,
) : CompetitionMessage()

@Component
class CompetitionCreateTeamRule(
    private val competitionPropertiesRepository: CompetitionPropertiesRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
) : CompetitionRule<CompetitionPlayer.Unknown, CompetitionCommand.CreateTeam, CompetitionCreateTeamMessage> {

    override suspend fun process(
        player: CompetitionPlayer.Unknown,
        command: CompetitionCommand.CreateTeam,
    ): List<CompGameMessage<CompetitionCreateTeamMessage>> {
        val properties =
            competitionPropertiesRepository.loadBySessionId(player.sessionId)

        val team = competitionTeamRepository.create(
            creator = player,
            name = command.teamName,
            maxTeams = properties.settings.maxTeamsAmount,
        )

        return buildResponse {
            team.id {
                +CompetitionCreateTeamMessage(
                    teamName = team.name,
                    idInGame = team.numberInGame,
                    captainEmail = team.captain.user.email,
                )
            }
        }
    }
}
