package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.repository.CompetitionPlayerRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository

data class CompetitionJoinTeamMessage(
    val teamName: String,
    val idInGame: Int,
    val teamMembers: List<String>,
) : CompetitionMessage()

@Component
class CompetitionJoinTeamRule(
    private val competitionPropertiesRepository: CompetitionPropertiesRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
    private val competitionPlayerRepository: CompetitionPlayerRepository,
) : CompetitionRule<CompetitionPlayer.Unknown, CompetitionCommand.JoinTeam, CompetitionJoinTeamMessage> {

    override suspend fun process(
        player: CompetitionPlayer.Unknown,
        command: CompetitionCommand.JoinTeam,
    ): List<CompGameMessage<CompetitionJoinTeamMessage>> {
        val properties =
            competitionPropertiesRepository.loadBySessionId(player.sessionId)

        val team = competitionTeamRepository
            .findByName(
                sessionId = player.sessionId,
                teamName = command.teamName,
            )
            ?: error("No team with name ${command.teamName}")

        val newMember = CompetitionPlayer.TeamMate(
            sessionId = player.sessionId,
            user = player.user,
            teamId = team.id,
        )
        competitionPlayerRepository.save(newMember, maxPlayers = properties.settings.maxTeamSize)

        return buildResponse {
            team.id {
                +CompetitionJoinTeamMessage(
                    teamName = command.teamName,
                    idInGame = 0,
                    teamMembers = team.teamMates.map { it.user.email },
                )
            }
        }
    }
}
