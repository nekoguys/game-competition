package ru.nekoguys.game.entity.competition.rule

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionSession
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
    ): List<CompGameMessage<CompetitionCreateTeamMessage>> = coroutineScope {
        val settings = async {
            competitionSessionRepository
                .load(player.sessionId, CompetitionSession.WithSettings)
                .settings
        }

        checkTeamDoesNotExists(player, command)

        val team = competitionTeamRepository.create(
            creator = player,
            name = command.teamName,
            password = command.password,
            maxTeams = settings.await().maxTeamsAmount,
        )

        buildResponse {
            team.id {
                +CompetitionCreateTeamMessage(
                    teamName = team.name,
                    idInGame = team.numberInGame,
                    captainEmail = player.user.email,
                )
            }
        }
    }

    private suspend fun checkTeamDoesNotExists(
        player: CompetitionPlayer.Unknown,
        command: CompetitionCommand.CreateTeam,
    ) {
        val existingTeam =
            competitionTeamRepository
                .findByName(
                    sessionId = player.sessionId,
                    teamName = command.teamName,
                )

        if (existingTeam != null) {
            throw CompetitionProcessException(
                "There is team with same name already"
            )
        }
    }
}
