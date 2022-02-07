package ru.nekoguys.game.entity.competition.rule

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.CompetitionProcessException
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
    ): List<CompGameMessage<CompetitionCreateTeamMessage>> = coroutineScope {
        val properties = async {
            competitionPropertiesRepository
                .loadBySessionId(player.sessionId)
        }

        val existingTeam = async {
            competitionTeamRepository
                .findByName(
                    sessionId = player.sessionId,
                    teamName = command.teamName,
                )
        }

        if (existingTeam.await() != null) {
            throw CompetitionProcessException(
                "There is team with same name already"
            )
        }

        val team = competitionTeamRepository.create(
            creator = player,
            name = command.teamName,
            password = command.password,
            maxTeams = properties.await().settings.maxTeamsAmount,
        )

        buildResponse {
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
